package com.hackathon.databeats.churninsight.infra.adapter.output.inference;

import ai.onnxruntime.*;
import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.exception.ModelInferenceException;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class OnnxRuntimeAdapter implements InferencePort {
    private final OrtEnvironment env;
    private final OrtSession session;
    private final ModelMetadata metadata;

    private static final List<String> PROBABILITY_OUTPUT_NAMES = List.of("output_probability", "probabilities", "probability");

    /**
     * CORREÇÃO APLICADA: O modelo foi treinado com classes invertidas.
     * Classe 0 = CHURN (cancelamento), Classe 1 = STAY (permanência)
     *
     * Com INVERT_CLASSES = true, corrigimos a interpretação para:
     * - probs[0] = probabilidade de WILL_STAY
     * - probs[1] = probabilidade de WILL_CHURN
     */
    private static final boolean INVERT_CLASSES = true;

    public OnnxRuntimeAdapter(InputStream modelStream, ModelMetadata metadata) throws OrtException, IOException {
        this.env = OrtEnvironment.getEnvironment();
        this.metadata = metadata;

        byte[] modelBytes = modelStream.readAllBytes();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();

        // OTIMIZAÇÃO: 1 thread por operação - evita competição quando usamos parallelStream
        // O paralelismo é controlado pelo Java (ForkJoinPool), não pelo ONNX
        options.setIntraOpNumThreads(1);
        options.setInterOpNumThreads(1);

        // Otimizações adicionais
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);

        this.session = env.createSession(modelBytes, options);

        // Log de inicialização do modelo
        log.info("✅ Modelo ONNX carregado com sucesso. INVERT_CLASSES={}", INVERT_CLASSES);
        if (log.isDebugEnabled()) {
            log.debug("Inputs esperados pelo modelo:");
            session.getInputInfo().forEach((name, info) ->
                log.debug(" - {}: {}", name, info.getInfo().toString()));
        }
    }

    /**
     * Versão Otimizada: Recebe as features calculadas para evitar overhead e inconsistência
     * OTIMIZAÇÃO: Minimiza alocações de objetos para melhor performance em batch
     */
    @Override
    public float[] predict(CustomerProfile profile, Map<String, Object> engineeredFeatures) {
        // Detecta automaticamente o modo de inferência pelo metadata
        if (metadata.isFlatTensorModel()) {
            return predictFlatTensor(profile, engineeredFeatures);
        }
        return predictNamedInputs(profile, engineeredFeatures);
    }

    /**
     * Modo flat tensor: XGBoost exportado com onnxmltools.
     * Detecta automaticamente se o modelo usa um único input "float_input" (tensor plano)
     * ou inputs nomeados por feature (ex: "age", "gender", etc.).
     */
    private float[] predictFlatTensor(CustomerProfile profile, Map<String, Object> engineeredFeatures) {
        List<String> featureOrder = metadata.getFeatureOrder();
        Map<String, Object> allValues = buildAllValuesMap(profile, engineeredFeatures);

        // Detecta o nome do primeiro input do modelo
        String firstInputName;
        try {
            firstInputName = session.getInputInfo().keySet().iterator().next();
        } catch (OrtException e) {
            throw new ModelInferenceException("Erro ao ler inputs do modelo ONNX: " + e.getMessage(), e);
        }

        if ("float_input".equals(firstInputName)) {
            // Modelo exportado com tensor plano único
            return predictWithSingleTensor(featureOrder, allValues);
        } else {
            // Modelo exportado com inputs nomeados por feature (onnxmltools preservou nomes do XGBoost)
            return predictWithNamedFloatTensors(featureOrder, allValues, profile);
        }
    }

    /**
     * Envia um único tensor float[1][N] com todas as features na ordem do feature_order.
     */
    private float[] predictWithSingleTensor(List<String> featureOrder, Map<String, Object> allValues) {
        float[] flatInput = new float[featureOrder.size()];
        for (int i = 0; i < featureOrder.size(); i++) {
            flatInput[i] = toFloat(allValues.get(featureOrder.get(i)), featureOrder.get(i));
        }
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, new float[][]{flatInput});
             OrtSession.Result result = session.run(Map.of("float_input", tensor))) {
            String outputName = findProbabilityOutputName(session.getOutputNames());
            OnnxValue probOutput = result.get(outputName)
                    .orElseThrow(() -> new ModelInferenceException("Output '" + outputName + "' não encontrado"));
            return extractProbabilities(probOutput);
        } catch (OrtException e) {
            throw new ModelInferenceException("Erro na inferência ONNX (flat tensor): " + e.getMessage(), e);
        }
    }

    /**
     * Envia cada feature como tensor nomeado individualmente.
     * Features numéricas → float[1][1], categóricas → String[1][1].
     * Usado quando onnxmltools preserva os nomes das features do XGBoost no ONNX.
     */
    private float[] predictWithNamedFloatTensors(List<String> featureOrder, Map<String, Object> allValues, CustomerProfile profile) {
        // Detecta quais inputs o modelo espera como string consultando o session
        Set<String> stringInputs = new java.util.HashSet<>();
        try {
            session.getInputInfo().forEach((name, nodeInfo) -> {
                var info = nodeInfo.getInfo();
                if (info instanceof ai.onnxruntime.TensorInfo ti) {
                    if (ti.type == ai.onnxruntime.OnnxJavaType.STRING) {
                        stringInputs.add(name);
                    }
                }
            });
        } catch (OrtException e) {
            // fallback: usa categorical_features do metadata
            stringInputs.addAll(metadata.getCategoricalFeatures());
        }

        OnnxTensor[] tensors = new OnnxTensor[featureOrder.size()];
        Map<String, OnnxTensor> inputs = new HashMap<>(featureOrder.size());
        int tensorIndex = 0;
        try {
            for (String feature : featureOrder) {
                if (stringInputs.contains(feature)) {
                    // Enviar como string normalizada (valor original do perfil)
                    String strVal = getCategoricalStringValue(feature, profile);
                    tensors[tensorIndex] = OnnxTensor.createTensor(env, new String[][]{{strVal}});
                } else {
                    float val = toFloat(allValues.get(feature), feature);
                    tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{val}});
                }
                inputs.put(feature, tensors[tensorIndex++]);
            }
            try (OrtSession.Result result = session.run(inputs)) {
                String outputName = findProbabilityOutputName(session.getOutputNames());
                OnnxValue probOutput = result.get(outputName)
                        .orElseThrow(() -> new ModelInferenceException("Output '" + outputName + "' não encontrado"));
                return extractProbabilities(probOutput);
            }
        } catch (OrtException e) {
            throw new ModelInferenceException("Erro na inferência ONNX (named tensors): " + e.getMessage(), e);
        } finally {
            for (int i = 0; i < tensorIndex; i++) {
                if (tensors[i] != null) tensors[i].close();
            }
        }
    }

    /** Retorna o valor string normalizado de uma feature categórica do perfil. */
    private String getCategoricalStringValue(String feature, CustomerProfile profile) {
        String raw = switch (feature) {
            case "gender" -> profile.gender();
            case "country" -> profile.country();
            case "subscription_type" -> profile.subscriptionType();
            case "device_type" -> profile.deviceType();
            default -> "";
        };
        return normalizeCategoricalValue(feature, raw);
    }

    /**
     * Constrói mapa com todos os valores do perfil + engineered features + categóricas encodadas.
     */
    private Map<String, Object> buildAllValuesMap(CustomerProfile profile, Map<String, Object> engineeredFeatures) {
        Map<String, Object> values = new HashMap<>();

        // Numéricas originais
        values.put("age", safeFloat(profile.age()));
        values.put("listening_time", (float) safeDouble(profile.listeningTime()));
        values.put("songs_played_per_day", safeInt(profile.songsPlayedPerDay()));
        values.put("skip_rate", (float) safeDouble(profile.skipRate()));
        values.put("ads_listened_per_week", safeInt(profile.adsListenedPerWeek()));
        values.put("offline_listening", (profile.offlineListening() != null && profile.offlineListening()) ? 1.0f : 0.0f);

        // Engineered features
        values.putAll(engineeredFeatures);

        // Categóricas — encodadas com LabelEncoder (inteiros) via label_encodings do metadata
        var labelEncodings = metadata.getLabelEncodings();
        if (labelEncodings != null) {
            encodeCategoric(values, "gender", profile.gender(), labelEncodings);
            encodeCategoric(values, "country", profile.country(), labelEncodings);
            encodeCategoric(values, "subscription_type", profile.subscriptionType(), labelEncodings);
            encodeCategoric(values, "device_type", profile.deviceType(), labelEncodings);
        }

        return values;
    }

    private void encodeCategoric(Map<String, Object> values, String feature, String rawValue,
                                  Map<String, Map<String, Integer>> labelEncodings) {
        String normalized = normalizeCategoricalValue(feature, rawValue);
        Map<String, Integer> encoding = labelEncodings.get(feature);
        if (encoding != null) {
            Integer encoded = encoding.get(normalized);
            values.put(feature, encoded != null ? encoded.floatValue() : 0.0f);
        } else {
            values.put(feature, 0.0f);
        }
    }

    private float toFloat(Object val, String featureName) {
        if (val == null) return 0f;
        if (val instanceof Boolean b) return b ? 1.0f : 0.0f;
        if (val instanceof Number n) return n.floatValue();
        return 0f;
    }

    /**
     * Modo inputs nomeados: modelo Logistic Regression (sklearn com named tensors).
     */
    private float[] predictNamedInputs(CustomerProfile profile, Map<String, Object> engineeredFeatures) {
        // Usa array local para inputs (mais eficiente que HashMap em hot path)
        OnnxTensor[] tensors = new OnnxTensor[16]; // máximo de inputs esperados
        Map<String, OnnxTensor> inputs = new HashMap<>(16);
        int tensorIndex = 0;

        try {
            // 1. Inputs Numéricos Originais - inline para evitar criação de Map intermediário
            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{safeFloat(profile.age())}});
            inputs.put("age", tensors[tensorIndex++]);

            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{(float) safeDouble(profile.listeningTime())}});
            inputs.put("listening_time", tensors[tensorIndex++]);

            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{safeInt(profile.songsPlayedPerDay())}});
            inputs.put("songs_played_per_day", tensors[tensorIndex++]);

            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{(float) safeDouble(profile.skipRate())}});
            inputs.put("skip_rate", tensors[tensorIndex++]);

            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{safeInt(profile.adsListenedPerWeek())}});
            inputs.put("ads_listened_per_week", tensors[tensorIndex++]);

            tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{(profile.offlineListening() != null && profile.offlineListening()) ? 1.0f : 0.0f}});
            inputs.put("offline_listening", tensors[tensorIndex++]);

            // 2. Inputs Numéricos Calculados (engineered features)
            for (Map.Entry<String, Object> entry : engineeredFeatures.entrySet()) {
                float val = 0f;
                Object value = entry.getValue();
                if (value instanceof Boolean b) {
                    val = b ? 1.0f : 0.0f;
                } else if (value instanceof Number n) {
                    val = n.floatValue();
                }
                tensors[tensorIndex] = OnnxTensor.createTensor(env, new float[][]{{val}});
                inputs.put(entry.getKey(), tensors[tensorIndex++]);
            }

            // 3. Inputs Categóricos
            for (String feature : this.metadata.getCategoricalFeatures()) {
                String rawValue = switch (feature) {
                    case "gender" -> profile.gender();
                    case "country" -> profile.country();
                    case "subscription_type" -> profile.subscriptionType();
                    case "device_type" -> profile.deviceType();
                    default -> "";
                };
                String treatedValue = normalizeCategoricalValue(feature, rawValue);
                tensors[tensorIndex] = OnnxTensor.createTensor(env, new String[][]{{treatedValue}});
                inputs.put(feature, tensors[tensorIndex++]);
            }

            // 4. Inferência
            try (OrtSession.Result result = session.run(inputs)) {
                String outputName = findProbabilityOutputName(session.getOutputNames());
                OnnxValue probOutput = result.get(outputName)
                        .orElseThrow(() -> new ModelInferenceException("Output '" + outputName + "' não encontrado"));
                return extractProbabilities(probOutput);
            }

        } catch (OrtException e) {
            throw new ModelInferenceException("Erro na inferência ONNX: " + e.getMessage(), e);
        } finally {
            // Limpeza de memória nativa - usa array para evitar iterator
            for (int i = 0; i < tensorIndex; i++) {
                if (tensors[i] != null) tensors[i].close();
            }
        }
    }

    // Sobrecarga para manter compatibilidade com predições unitárias simples se necessário
    public float[] predict(CustomerProfile profile) {
        return predict(profile, new HashMap<>());
    }

    private String findProbabilityOutputName(Set<String> outputNames) {
        for (String name : PROBABILITY_OUTPUT_NAMES) {
            if (outputNames.contains(name)) {
                return name;
            }
        }
        if (outputNames.size() > 1) {
            Iterator<String> it = outputNames.iterator();
            it.next(); // skip first
            return it.next(); // return second
        }
        return outputNames.iterator().next();
    }

    private float[] extractProbabilities(OnnxValue probOutput) throws OrtException {
        Object probValue = probOutput.getValue();

        float[] result = switch (probValue) {
            case long[] longArray -> {
                float val = (float) longArray[0];
                yield new float[]{1 - val, val};
            }
            case float[] floatArray -> floatArray;
            case float[][] floatArrayArray -> floatArrayArray[0];
            case List<?> list -> listToFloatArray(list);
            case OnnxMap onnxMap -> {
                if (onnxMap.getValue() instanceof Map<?, ?> map) yield mapToFloatArray(map);
                throw new ModelInferenceException("Tipo de mapa inesperado.");
            }
            case OnnxSequence seq -> {
                if (seq.getValue() instanceof List<?> list) yield listToFloatArray(list);
                throw new ModelInferenceException("Tipo de sequência inesperada.");
            }
            default -> throw new ModelInferenceException("Formato inesperado: " + probValue.getClass().getName());
        };

        if (result.length == 1) {
            float pChurn = result[0];
            return new float[]{1.0f - pChurn, pChurn};
        }

        // Se vier [p0, p1] e o modelo estiver invertido, inverte SEMPRE (map e array)
        if (result.length >= 2 && INVERT_CLASSES) {
            return new float[]{result[1], result[0]};
        }

        return result;
    }

    private float[] listToFloatArray(List<?> list) throws OrtException {
        if (!list.isEmpty()) {
            Object firstElement = list.getFirst();

            // Verifica se é um OnnxMap (estrutura comum para probabilidades em sklearn-onnx)
            if (firstElement instanceof OnnxMap onnxMap) {
                Object mapValue = onnxMap.getValue();
                if (mapValue instanceof Map<?, ?> map) {
                    return mapToFloatArray(map);
                }
            }

            // Fallback para Map Java padrão
            if (firstElement instanceof Map) {
                return mapToFloatArray((Map<?, ?>) firstElement);
            }
        }

        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = toFloat(list.get(i));
        }
        return arr;
    }

    private float[] mapToFloatArray(Map<?, ?> map) {
        float p0 = 0f, p1 = 0f;

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object val = entry.getValue();
            if (isIndexZero(key)) {
                p0 = toFloat(val);
            } else if (isIndexOne(key)) {
                p1 = toFloat(val);
            }
        }

        // Aplica inversão se o modelo foi treinado com classes invertidas
        if (INVERT_CLASSES) {
            // Modelo invertido: p0=CHURN, p1=STAY → retorna [p1, p0] para [STAY, CHURN]
            return new float[]{p1, p0};
        } else {
            // Convenção padrão sklearn: p0=STAY, p1=CHURN
            return new float[]{p0, p1};
        }
    }

    private boolean isIndexZero(Object key) {
        if (key instanceof Number n) return n.intValue() == 0;
        if (key instanceof String s) return s.equals("0") || s.equalsIgnoreCase("WILL_STAY");
        return false;
    }

    private boolean isIndexOne(Object key) {
        if (key instanceof Number n) return n.intValue() == 1;
        if (key instanceof String s) return s.equals("1") || s.equalsIgnoreCase("WILL_CHURN");
        return false;
    }

    private float toFloat(Object val) {
        if (val == null) return 0f;
        if (val instanceof Number n) return n.floatValue();
        return 0f;
    }

    /**
     * Normaliza valores categóricos para o formato esperado pelo modelo treinado.
     * O modelo foi treinado com valores específicos (ex: "Male", "Free", "DE", "Desktop").
     */
    private String normalizeCategoricalValue(String feature, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }

        String value = rawValue.trim().toLowerCase();

        return switch (feature) {
            case "gender" -> switch (value) {
                case "male", "m", "masculino" -> "Male";
                case "female", "f", "feminino" -> "Female";
                case "other", "outro", "outros" -> "Other";
                default -> capitalizeFirst(rawValue);
            };
            case "country" -> normalizeCountry(value);
            case "subscription_type" -> switch (value) {
                case "free", "gratis", "gratuito" -> "Free";
                case "premium", "pago" -> "Premium";
                case "family", "familia", "familiar" -> "Family";
                case "student", "estudante", "universitario" -> "Student";
                default -> capitalizeFirst(rawValue);
            };
            case "device_type" -> switch (value) {
                case "desktop", "computer", "pc", "computador" -> "Desktop";
                case "mobile", "celular", "smartphone", "phone" -> "Mobile";
                case "web", "browser", "navegador" -> "Web";
                default -> capitalizeFirst(rawValue);
            };
            default -> rawValue.trim();
        };
    }

    /**
     * Normaliza nomes de países para códigos ISO de 2 letras (formato do modelo)
     */
    private String normalizeCountry(String value) {
        return switch (value) {
            case "germany", "de", "alemanha", "deutschland" -> "DE";
            case "usa", "us", "united states", "estados unidos", "eua" -> "US";
            case "brazil", "br", "brasil" -> "BR";
            case "canada", "ca", "canadá" -> "CA";
            case "uk", "gb", "united kingdom", "reino unido", "england", "inglaterra" -> "GB";
            case "france", "fr", "frança" -> "FR";
            case "spain", "es", "espanha", "españa" -> "ES";
            case "italy", "it", "italia", "itália" -> "IT";
            case "portugal", "pt" -> "PT";
            case "mexico", "mx", "méxico" -> "MX";
            case "argentina", "ar" -> "AR";
            default -> value.toUpperCase(); // Assume que é um código ISO
        };
    }

    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private float safeFloat(Integer value) { return value == null ? 0f : value.floatValue(); }
    private double safeDouble(Double value) { return value == null ? 0d : value; }
    private float safeInt(Integer value) { return value == null ? 0f : value.floatValue(); }

    @Override
    public boolean isModelLoaded() { return session != null; }
}