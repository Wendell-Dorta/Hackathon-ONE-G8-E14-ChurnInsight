package com.hackathon.databeats.churninsight.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representando o contrato da API de predição.
 *
 * <p>Carregado do arquivo {@code contrato_api.json} gerado pelo pipeline de ML.
 * Fornece especificação completa para documentação, validação e testes.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record ApiContract(

        /** Endpoint da API (ex: /predict). */
        String endpoint,

        /** Metadados do modelo. */
        Metadata metadata,

        /** Schema do payload de entrada. */
        @JsonProperty("payload_input")
        PayloadInput payloadInput,

        /** Schema da resposta esperada. */
        @JsonProperty("expected_response")
        ExpectedResponse expectedResponse
) {

    // Compatibility getters
    public String getEndpoint() {
        return endpoint;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public PayloadInput getPayloadInput() {
        return payloadInput;
    }

    public ExpectedResponse getExpectedResponse() {
        return expectedResponse;
    }

    /**
     * Metadados do modelo de ML.
     */
    public static record Metadata(
            @JsonProperty("model_name") String modelName,
            @JsonProperty("model_version") String modelVersion,
            @JsonProperty("api_standard") String apiStandard
    ) {
        // compatibility getters
        public String getModelName() {
            return modelName;
        }

        public String getModelVersion() {
            return modelVersion;
        }

        public String getApiStandard() {
            return apiStandard;
        }
    }

    /**
     * Schema de entrada do payload.
     */
    public static record PayloadInput(
            String gender,
            Integer age,
            String country,
            @JsonProperty("subscription_type") String subscriptionType,
            @JsonProperty("listening_time") Integer listeningTime,
            @JsonProperty("songs_played_per_day") Integer songsPlayedPerDay,
            @JsonProperty("skip_rate") Double skipRate,
            @JsonProperty("device_type") String deviceType,
            @JsonProperty("ads_listened_per_week") Integer adsListenedPerWeek,
            @JsonProperty("offline_listening") Integer offlineListening,
            @JsonProperty("songs_per_minute") Double songsPerMinute,
            @JsonProperty("ad_intensity") Double adIntensity,
            @JsonProperty("frustration_index") Double frustrationIndex,
            @JsonProperty("is_heavy_user") Integer isHeavyUser,
            @JsonProperty("premium_no_offline") Integer premiumNoOffline
    ) {
        // compatibility getters
        public String getGender() {
            return gender;
        }

        public Integer getAge() {
            return age;
        }

        public String getCountry() {
            return country;
        }

        public String getSubscriptionType() {
            return subscriptionType;
        }

        public Integer getListeningTime() {
            return listeningTime;
        }

        public Integer getSongsPlayedPerDay() {
            return songsPlayedPerDay;
        }

        public Double getSkipRate() {
            return skipRate;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public Integer getAdsListenedPerWeek() {
            return adsListenedPerWeek;
        }

        public Integer getOfflineListening() {
            return offlineListening;
        }

        public Double getSongsPerMinute() {
            return songsPerMinute;
        }

        public Double getAdIntensity() {
            return adIntensity;
        }

        public Double getFrustrationIndex() {
            return frustrationIndex;
        }

        public Integer getIsHeavyUser() {
            return isHeavyUser;
        }

        public Integer getPremiumNoOffline() {
            return premiumNoOffline;
        }
    }

    /**
     * Schema da resposta esperada.
     */
    public static record ExpectedResponse(
            String prediction,
            Double probability,
            @JsonProperty("decision_threshold") Double decisionThreshold,
            @JsonProperty("ai_diagnosis") AiDiagnosis aiDiagnosis
    ) {
        public String getPrediction() {
            return prediction;
        }

        public Double getProbability() {
            return probability;
        }

        public Double getDecisionThreshold() {
            return decisionThreshold;
        }

        public AiDiagnosis getAiDiagnosis() {
            return aiDiagnosis;
        }
    }

    /**
     * Diagnóstico gerado por IA.
     */
    public static record AiDiagnosis(
            @JsonProperty("primary_risk_factor") String primaryRiskFactor,
            @JsonProperty("primary_retention_factor") String primaryRetentionFactor,
            @JsonProperty("suggested_action") String suggestedAction
    ) {
        public String getPrimaryRiskFactor() {
            return primaryRiskFactor;
        }

        public String getPrimaryRetentionFactor() {
            return primaryRetentionFactor;
        }

        public String getSuggestedAction() {
            return suggestedAction;
        }
    }
}