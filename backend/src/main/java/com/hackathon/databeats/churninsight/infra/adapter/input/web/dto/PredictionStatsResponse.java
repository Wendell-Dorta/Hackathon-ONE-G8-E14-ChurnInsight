package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;

import java.util.Map;

/**
 * DTO de resposta para estatísticas detalhadas de predição.
 *
 * <p>Utilizado pelo endpoint /stats para retornar informações completas
 * sobre a predição, incluindo probabilidades por classe.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record PredictionStatsResponse(
    ChurnStatus label,
    double probability,
    float[] probabilities,
    Map<String, Float> classProbabilities
) {

    public ChurnStatus getLabel() { return label; }
    public double getProbability() { return probability; }
    public float[] getProbabilities() { return probabilities; }
    public Map<String, Float> getClassProbabilities() { return classProbabilities; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private ChurnStatus label;
        private double probability;
        private float[] probabilities;
        private Map<String, Float> classProbabilities;

        public Builder label(ChurnStatus label) { this.label = label; return this; }
        public Builder probability(double probability) { this.probability = probability; return this; }
        public Builder probabilities(float[] probabilities) { this.probabilities = probabilities; return this; }
        public Builder classProbabilities(Map<String, Float> classProbabilities) { this.classProbabilities = classProbabilities; return this; }
        public PredictionStatsResponse build() { return new PredictionStatsResponse(label, probability, probabilities, classProbabilities); }
    }
}