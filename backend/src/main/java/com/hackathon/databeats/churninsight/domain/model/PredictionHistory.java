package com.hackathon.databeats.churninsight.domain.model;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Modelo de domínio que representa o histórico de uma predição de churn.
 */
public record PredictionHistory(
    String id,
    String userId,
    String gender,
    Integer age,
    String country,
    String subscriptionType,
    String deviceType,
    Double listeningTime,
    Integer songsPlayedPerDay,
    Double skipRate,
    Integer adsListenedPerWeek,
    Boolean offlineListening,
    ChurnStatus churnStatus,
    Double probability,
    Double frustrationIndex,
    Double adIntensity,
    Double songsPerMinute,
    Boolean isHeavyUser,
    Boolean premiumNoOffline,
    String requesterId,
    String requestIp,
    LocalDateTime createdAt
) {
    public static PredictionHistory fromPrediction(
            String id,
            CustomerProfile profile,
            ChurnStatus status,
            double probability,
            Map<String, Object> engineeredFeatures,
            String requesterId,
            String requestIp) {

        return PredictionHistory.builder()
                .id(id)
                .userId(profile.userId())
                .gender(profile.gender())
                .age(profile.age())
                .country(profile.country())
                .subscriptionType(profile.subscriptionType())
                .deviceType(profile.deviceType())
                .listeningTime(profile.listeningTime())
                .songsPlayedPerDay(profile.songsPlayedPerDay())
                .skipRate(profile.skipRate())
                .adsListenedPerWeek(profile.adsListenedPerWeek())
                .offlineListening(profile.offlineListening())
                .churnStatus(status)
                .probability(probability)
                .frustrationIndex(extractDouble(engineeredFeatures, "frustration_index"))
                .adIntensity(extractDouble(engineeredFeatures, "ad_intensity"))
                .songsPerMinute(extractDouble(engineeredFeatures, "songs_per_minute"))
                .isHeavyUser(extractBoolean(engineeredFeatures, "is_heavy_user"))
                .premiumNoOffline(extractBoolean(engineeredFeatures, "premium_no_offline"))
                .requesterId(requesterId)
                .requestIp(requestIp)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // legacy compatibility Builder
    public static Builder builder() { return new Builder(); }
    public static class Builder {
        private String id;
        private String userId;
        private String gender;
        private Integer age;
        private String country;
        private String subscriptionType;
        private String deviceType;
        private Double listeningTime;
        private Integer songsPlayedPerDay;
        private Double skipRate;
        private Integer adsListenedPerWeek;
        private Boolean offlineListening;
        private ChurnStatus churnStatus;
        private Double probability;
        private Double frustrationIndex;
        private Double adIntensity;
        private Double songsPerMinute;
        private Boolean isHeavyUser;
        private Boolean premiumNoOffline;
        private String requesterId;
        private String requestIp;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder subscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder listeningTime(Double listeningTime) { this.listeningTime = listeningTime; return this; }
        public Builder songsPlayedPerDay(Integer songsPlayedPerDay) { this.songsPlayedPerDay = songsPlayedPerDay; return this; }
        public Builder skipRate(Double skipRate) { this.skipRate = skipRate; return this; }
        public Builder adsListenedPerWeek(Integer adsListenedPerWeek) { this.adsListenedPerWeek = adsListenedPerWeek; return this; }
        public Builder offlineListening(Boolean offlineListening) { this.offlineListening = offlineListening; return this; }
        public Builder churnStatus(ChurnStatus churnStatus) { this.churnStatus = churnStatus; return this; }
        public Builder probability(Double probability) { this.probability = probability; return this; }
        public Builder frustrationIndex(Double frustrationIndex) { this.frustrationIndex = frustrationIndex; return this; }
        public Builder adIntensity(Double adIntensity) { this.adIntensity = adIntensity; return this; }
        public Builder songsPerMinute(Double songsPerMinute) { this.songsPerMinute = songsPerMinute; return this; }
        public Builder isHeavyUser(Boolean isHeavyUser) { this.isHeavyUser = isHeavyUser; return this; }
        public Builder premiumNoOffline(Boolean premiumNoOffline) { this.premiumNoOffline = premiumNoOffline; return this; }
        public Builder requesterId(String requesterId) { this.requesterId = requesterId; return this; }
        public Builder requestIp(String requestIp) { this.requestIp = requestIp; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PredictionHistory build() { return new PredictionHistory(id, userId, gender, age, country, subscriptionType, deviceType, listeningTime, songsPlayedPerDay, skipRate, adsListenedPerWeek, offlineListening, churnStatus, probability, frustrationIndex, adIntensity, songsPerMinute, isHeavyUser, premiumNoOffline, requesterId, requestIp, createdAt); }
    }

    private static Double extractDouble(Map<String, Object> features, String key) {
        Object value = features.get(key);
        if (value instanceof Number number) return number.doubleValue();
        return null;
    }

    private static Boolean extractBoolean(Map<String, Object> features, String key) {
        Object value = features.get(key);
        if (value instanceof Boolean b) return b;
        return null;
    }
}
