package com.hackathon.databeats.churninsight.domain.model;

import java.util.Objects;

/**
 * Value Object de domínio puro.
 * Representa o estado imutável de um perfil de cliente.
 */
public record CustomerProfile(
        String gender,
        Integer age,
        String country,
        String subscriptionType,
        Double listeningTime,
        Integer songsPlayedPerDay,
        Double skipRate,
        Integer adsListenedPerWeek,
        String deviceType,
        Boolean offlineListening,
        String userId
) {

    /**
     * Construtor Compacto (Canonical Constructor).
     * Local ideal para validações de Regra de Negócio que devem ser
     * respeitadas em qualquer lugar do sistema.
     */
    public CustomerProfile {
        Objects.requireNonNull(userId, "UserId é obrigatório");
        Objects.requireNonNull(gender, "Gender é obrigatório");

        if (age == null || age < 10 || age > 120) {
            throw new IllegalArgumentException("Idade deve estar entre 10 e 120 anos");
        }

        if (skipRate != null && (skipRate < 0.0 || skipRate > 1.0)) {
            throw new IllegalArgumentException("SkipRate deve estar entre 0.0 e 1.0");
        }

        // Padronização de dados (Opcional, mas útil no domínio)
        country = (country != null) ? country.toUpperCase() : null;
    }

    /**
     * Lógica de domínio: identifica se o cliente tem acesso a recursos premium.
     */
    public boolean isPremium() {
        return "premium".equalsIgnoreCase(subscriptionType);
    }

    /**
     * Facilita a criação do objeto em Testes e Services de Aplicação.
     */
    public static Builder builder() {
        return new Builder();
    }

    // Builder estático manual para evitar dependência de bibliotecas externas no domínio
    public static class Builder {
        private String gender;
        private Integer age;
        private String country;
        private String subscriptionType;
        private Double listeningTime;
        private Integer songsPlayedPerDay;
        private Double skipRate;
        private Integer adsListenedPerWeek;
        private String deviceType;
        private Boolean offlineListening;
        private String userId;

        public Builder gender(String gender) { this.gender = gender; return this; }
        public Builder age(Integer age) { this.age = age; return this; }
        public Builder country(String country) { this.country = country; return this; }
        public Builder subscriptionType(String subscriptionType) { this.subscriptionType = subscriptionType; return this; }
        public Builder listeningTime(Double listeningTime) { this.listeningTime = listeningTime; return this; }
        public Builder songsPlayedPerDay(Integer songsPlayedPerDay) { this.songsPlayedPerDay = songsPlayedPerDay; return this; }
        public Builder skipRate(Double skipRate) { this.skipRate = skipRate; return this; }
        public Builder adsListenedPerWeek(Integer adsListenedPerWeek) { this.adsListenedPerWeek = adsListenedPerWeek; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder offlineListening(Boolean offlineListening) { this.offlineListening = offlineListening; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }

        public CustomerProfile build() {
            return new CustomerProfile(
                    gender, age, country, subscriptionType, listeningTime,
                    songsPlayedPerDay, skipRate, adsListenedPerWeek, deviceType,
                    offlineListening, userId
            );
        }
    }
}