package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

import java.util.Map;

/**
 * Port de saída para inferência do modelo de Machine Learning.
 *
 * <p>Define o contrato para execução de predições, permitindo que a camada de aplicação
 * seja independente da implementação específica (ONNX, TensorFlow, PyTorch, etc).</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface InferencePort {

    /**
     * Executa predição de churn para um perfil de cliente.
     *
     * @param profile perfil do cliente com features de entrada
     * @param engineeredFeatures features calculadas pelo serviço de domínio
     * @return array com probabilidades [P(stay), P(churn)]
     */
    float[] predict(CustomerProfile profile, Map<String, Object> engineeredFeatures);

    /**
     * Verifica se o modelo está carregado e funcional.
     *
     * @return {@code true} se o modelo pode executar predições
     */
    boolean isModelLoaded();
}

