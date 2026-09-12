package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter;

import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Adapter de persistência MySQL.
 * Converte modelo de domínio para entidade JPA, seguindo arquitetura hexagonal.
 */
@Component
@Slf4j
public class MySQLHistoryAdapter implements SaveHistoryPort {
    private final PredictionHistoryRepository repository;

    public MySQLHistoryAdapter(PredictionHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(PredictionHistory history) {
        PredictionHistoryEntity entity = toEntity(history);
        this.repository.save(java.util.Objects.requireNonNull(entity, "PredictionHistoryEntity must not be null"));
    }

    /**
     * Converte modelo de domínio para entidade JPA.
     * Esta conversão é responsabilidade do adapter, não do domínio.
     */
    private PredictionHistoryEntity toEntity(PredictionHistory history) {
        PredictionHistoryEntity entity = new PredictionHistoryEntity();

        entity.setId(history.id());
        entity.setUserId(history.userId());
        entity.setGender(history.gender());
        entity.setAge(history.age() != null ? history.age() : 0);
        entity.setCountry(history.country());
        entity.setSubscriptionType(history.subscriptionType());
        entity.setDeviceType(history.deviceType());
        entity.setListeningTime(history.listeningTime() != null ? history.listeningTime() : 0.0);
        entity.setSongsPlayedPerDay(history.songsPlayedPerDay() != null ? history.songsPlayedPerDay() : 0);
        entity.setSkipRate(history.skipRate() != null ? history.skipRate() : 0.0);
        entity.setAdsListenedPerWeek(history.adsListenedPerWeek() != null ? history.adsListenedPerWeek() : 0);
        entity.setOfflineListening(history.offlineListening() != null && history.offlineListening());

        entity.setChurnStatus(history.churnStatus());
        entity.setProbability(history.probability() != null ? history.probability() : 0.0);

        entity.setFrustrationIndex(history.frustrationIndex());
        entity.setAdIntensity(history.adIntensity());
        entity.setSongsPerMinute(history.songsPerMinute());
        entity.setIsHeavyUser(history.isHeavyUser());
        entity.setPremiumNoOffline(history.premiumNoOffline());

        entity.setRequesterId(history.requesterId());
        entity.setRequestIp(history.requestIp());
        entity.setCreatedAt(history.createdAt());

        return entity;
    }
}