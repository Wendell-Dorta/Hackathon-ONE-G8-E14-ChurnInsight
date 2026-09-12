package com.hackathon.databeats.churninsight.application.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;

import java.time.LocalDateTime;

/**
 * DTO de resposta para consulta de histórico de predições.
 *
 * <p>Versão resumida do {@link com.hackathon.databeats.churninsight.domain.model.PredictionHistory}
 * otimizada para listagem e visualização em telas/APIs. Omite dados brutos/internos.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record PredictionHistoryResponse(

    /** Identificador único da predição (UUIDv7). */
    String id,

    /** ID do cliente no sistema externo. */
    String userId,

    /** Gênero do cliente. */
    String gender,

    /** Idade do cliente em anos. */
    int age,

    /** País de origem (ISO code). */
    String country,

    /** Tipo de assinatura. */
    String subscriptionType,

    /** Tipo de dispositivo primário. */
    String deviceType,

    /** Status de classificação de churn. */
    ChurnStatus churnStatus,

    /** Probabilidade de churn (0.0 a 1.0). */
    double probability,

    /** Rótulo legível da predição ("Vai Cancelar" ou "Vai Continuar"). */
    String predictionLabel,

    /** Índice de frustração calculado (0.0+). */
    Double frustrationIndex,

    /** {@code true} se cliente é classificado como usuário intenso. */
    Boolean isHeavyUser,

    /** Ação recomendada para time de retenção (ex: "Oferecer desconto", "Ligar"). */
    String recommendedAction,

    /** Fator de risco primário identificado. */
    String primaryRiskFactor,

    /** Timestamp da execução da predição. */
    LocalDateTime createdAt
) {
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String id;
        private String userId;
        private String gender;
        private int age;
        private String country;
        private String subscriptionType;
        private String deviceType;
        private ChurnStatus churnStatus;
        private double probability;
        private String predictionLabel;
        private Double frustrationIndex;
        private Boolean isHeavyUser;
        private String recommendedAction;
        private String primaryRiskFactor;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder age(int age) { this.age = age; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder subscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder churnStatus(ChurnStatus churnStatus) { this.churnStatus = churnStatus; return this; }
        public Builder probability(double probability) { this.probability = probability; return this; }
        public Builder predictionLabel(String predictionLabel) { this.predictionLabel = predictionLabel; return this; }
        public Builder frustrationIndex(Double frustrationIndex) { this.frustrationIndex = frustrationIndex; return this; }
        public Builder isHeavyUser(Boolean isHeavyUser) { this.isHeavyUser = isHeavyUser; return this; }
        public Builder recommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; return this; }
        public Builder primaryRiskFactor(String primaryRiskFactor) { this.primaryRiskFactor = primaryRiskFactor; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PredictionHistoryResponse build() { return new PredictionHistoryResponse(id, userId, gender, age, country, subscriptionType, deviceType, churnStatus, probability, predictionLabel, frustrationIndex, isHeavyUser, recommendedAction, primaryRiskFactor, createdAt); }
    }
}
