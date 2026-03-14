package com.hackathon.databeats.churninsight.infra.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import lombok.Data;

import java.util.List;

/**
 * Implementação dos metadados do modelo de ML carregados do arquivo JSON.
 *
 * <p>Contém as métricas de performance, configurações de features e
 * parâmetros do modelo ONNX exportado pelo time de Data Science.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelMetadata implements ModelMetadataPort {

    /** Nome identificador do modelo. */
    private String name;

    /** Versão do modelo no formato semântico. */
    private String version;

    /** Acurácia do modelo (0.0 a 1.0). */
    private double accuracy;

    /** F1-Score do modelo (0.0 a 1.0). */
    @JsonProperty(value = "f1_score")
    private double f1Score;

    @JsonProperty(value = "features")
    private List<String> features;

    @JsonProperty(value = "numeric_features")
    private List<String> numericFeatures;

    @JsonProperty(value = "categorical_features")
    private List<String> categoricalFeatures;

    // Métodos utilitários para manter compatibilidade com a interface ModelMetadataPort
    public List<String> getNumericFeatures() {
        if (numericFeatures != null && !numericFeatures.isEmpty()) return numericFeatures;
        return features != null ? features : List.of();
    }

    public List<String> getCategoricalFeatures() {
        if (categoricalFeatures != null && !categoricalFeatures.isEmpty()) return categoricalFeatures;
        return List.of();
    }

    /** Threshold genérico do modelo (campo extra do JSON). */
    @JsonProperty(value = "threshold")
    private Double threshold;

    /** Tipo do algoritmo utilizado (ex: Logistic Regression with SMOTE). */
    @JsonProperty(value = "model_type")
    private String modelType;

    /** Precisão do modelo (0.0 a 1.0). */
    private double precision;

    /** Recall do modelo (0.0 a 1.0). */
    private double recall;

    /** Área sob a curva ROC (0.0 a 1.0). */
    @JsonProperty(value = "auc_roc")
    private double aucRoc;

    /** Threshold ótimo de classificação calculado durante o treino. */
    @JsonProperty(value = "threshold_otimo")
    private double thresholdOtimo;

    /** Data de exportação do modelo. */
    @JsonProperty(value = "export_date")
    private String exportDate;

    /**
     * Ordem exata das features no flat tensor (modelo XGBoost exportado com onnxmltools).
     * Presente apenas quando model_type = XGBoost.
     */
    @JsonProperty(value = "feature_order")
    private List<String> featureOrder;

    /**
     * Mapeamento LabelEncoder para features categóricas.
     * Ex: {"gender": {"Male": 1, "Female": 0, "Other": 2}}
     */
    @JsonProperty(value = "label_encodings")
    private java.util.Map<String, java.util.Map<String, Integer>> labelEncodings;

    /** Retorna true se o modelo usa flat tensor (XGBoost via onnxmltools). */
    public boolean isFlatTensorModel() {
        return featureOrder != null && !featureOrder.isEmpty();
    }

    @Override
    public String getNomeModelo() {
        return this.name;
    }

    @Override
    public String getVersaoModelo() {
        return this.version;
    }

    @Override
    public double getAcuracia() {
        return this.accuracy;
    }

    @Override
    public double getPrecisao() {
        return this.precision;
    }
}