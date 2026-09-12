package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper;

import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class PredictionHistoryMapper {

    // Converte DOMÍNIO -> ENTIDADE (Para salvar no banco)
    public PredictionHistoryEntity toEntity(PredictionHistory domain) {
        if (domain == null) return null;

        PredictionHistoryEntity entity = new PredictionHistoryEntity();

        // Mapeamento 1 para 1
        entity.setId(domain.id());
        entity.setUserId(domain.userId());
        entity.setGender(domain.gender());
        entity.setAge(domain.age());
        entity.setCountry(domain.country());
        entity.setSubscriptionType(domain.subscriptionType());
        entity.setDeviceType(domain.deviceType());
        entity.setListeningTime(domain.listeningTime() != null ? domain.listeningTime() : 0.0);
        entity.setSongsPlayedPerDay(domain.songsPlayedPerDay() != null ? domain.songsPlayedPerDay() : 0);
        entity.setSkipRate(domain.skipRate() != null ? domain.skipRate() : 0.0);
        entity.setAdsListenedPerWeek(domain.adsListenedPerWeek() != null ? domain.adsListenedPerWeek() : 0);
        entity.setOfflineListening(domain.offlineListening() != null ? domain.offlineListening() : false);

        entity.setChurnStatus(domain.churnStatus());
        entity.setProbability(domain.probability() != null ? domain.probability() : 0.0);

        entity.setFrustrationIndex(domain.frustrationIndex());
        entity.setAdIntensity(domain.adIntensity());
        entity.setSongsPerMinute(domain.songsPerMinute());
        entity.setIsHeavyUser(domain.isHeavyUser());
        entity.setPremiumNoOffline(domain.premiumNoOffline());

        entity.setRequesterId(domain.requesterId());
        entity.setRequestIp(domain.requestIp());

        // createdAt geralmente é gerado pelo banco ou na entity,
        // mas se o domínio já definiu, respeitamos:
        if (domain.createdAt() != null) {
            entity.setCreatedAt(domain.createdAt());
        }

        return entity;
    }

    // Converte ENTIDADE -> DOMÍNIO (Para ler do banco e devolver pra regra de negócio/API)
    public PredictionHistory toDomain(PredictionHistoryEntity entity) {
        if (entity == null) return null;

        return PredictionHistory.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .gender(entity.getGender())
                .age(entity.getAge())
                .country(entity.getCountry())
                .subscriptionType(entity.getSubscriptionType())
                .deviceType(entity.getDeviceType())
                .listeningTime(entity.getListeningTime())
                .songsPlayedPerDay(entity.getSongsPlayedPerDay())
                .skipRate(entity.getSkipRate())
                .adsListenedPerWeek(entity.getAdsListenedPerWeek())
                .offlineListening(entity.isOfflineListening())
                .churnStatus(entity.getChurnStatus())
                .probability(entity.getProbability())
                .frustrationIndex(entity.getFrustrationIndex())
                .adIntensity(entity.getAdIntensity())
                .songsPerMinute(entity.getSongsPerMinute())
                .isHeavyUser(entity.getIsHeavyUser())
                .premiumNoOffline(entity.getPremiumNoOffline())
                .requesterId(entity.getRequesterId())
                .requestIp(entity.getRequestIp())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}