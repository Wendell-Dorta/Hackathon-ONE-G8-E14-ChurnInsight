package com.hackathon.databeats.churninsight.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representando uma predição de cliente pré-calculada do modelo ONNX.
 * Carregado do arquivo clients.json gerado pelo pipeline de ML.
 */
public record ClientPrediction(

    @JsonProperty("clientId")
    String clientId,

    @JsonProperty("probability")
    double probability,

    @JsonProperty("prediction")
    String prediction,

    @JsonProperty("primary_risk_factor")
    String primaryRiskFactor,

    @JsonProperty("primary_retention_factor")
    String primaryRetentionFactor
) {

    /**
     * Verifica se o cliente vai cancelar baseado na predição
     */
    public boolean willChurn() {
        return "Vai Cancelar".equalsIgnoreCase(prediction);
    }
}
