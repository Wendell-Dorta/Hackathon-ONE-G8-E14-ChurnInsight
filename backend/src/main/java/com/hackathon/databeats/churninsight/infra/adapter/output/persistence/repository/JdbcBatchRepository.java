package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.application.port.output.BatchSavePort;
import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class JdbcBatchRepository implements BatchSavePort {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
        INSERT INTO churn_history (
            id, user_id, gender, age, country, subscription_type, 
            listening_time, songs_played_per_day, skip_rate, 
            ads_listened_per_week, device_type, offline_listening, 
            churn_status, probability, created_at, requester_id, request_ip,
            frustration_index, ad_intensity, songs_per_minute, 
            is_heavy_user, premium_no_offline
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    @Transactional
    public void saveAll(List<PredictionHistory> histories) {
        saveBatch(histories, 10000);
    }

    @Override
    @Transactional
    public void saveBatch(List<PredictionHistory> histories, int batchSize) {
        if (histories.isEmpty()) return;

        try {
            jdbcTemplate.batchUpdate(INSERT_SQL, histories, batchSize,
                    (PreparedStatement ps, PredictionHistory h) -> {
                        ps.setString(1, h.id());
                        ps.setString(2, h.userId());
                        ps.setString(3, h.gender());
                        ps.setInt(4, h.age() != null ? h.age() : 0);
                        ps.setString(5, h.country());
                        ps.setString(6, h.subscriptionType());
                        ps.setDouble(7, h.listeningTime() != null ? h.listeningTime() : 0.0);
                        ps.setInt(8, h.songsPlayedPerDay() != null ? h.songsPlayedPerDay() : 0);
                        ps.setDouble(9, h.skipRate() != null ? h.skipRate() : 0.0);
                        ps.setInt(10, h.adsListenedPerWeek() != null ? h.adsListenedPerWeek() : 0);
                        ps.setString(11, h.deviceType());
                        ps.setBoolean(12, h.offlineListening() != null && h.offlineListening());
                        ps.setString(13, h.churnStatus() != null ? h.churnStatus().name() : "UNKNOWN");
                        ps.setDouble(14, h.probability() != null ? h.probability() : 0.0);
                        ps.setTimestamp(15, h.createdAt() != null ? Timestamp.valueOf(h.createdAt()) : Timestamp.valueOf(LocalDateTime.now()));
                        ps.setString(16, h.requesterId());
                        ps.setString(17, h.requestIp());
                        ps.setObject(18, h.frustrationIndex());
                        ps.setObject(19, h.adIntensity());
                        ps.setObject(20, h.songsPerMinute());
                        ps.setObject(21, h.isHeavyUser());
                        ps.setObject(22, h.premiumNoOffline());
                    });
        } catch (Exception e) {
            log.error("Erro ao salvar no banco via JDBC: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public long countTotalPredictions() {
        // CORREÇÃO: Mudei para churn_history para bater com sua @Table
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM churn_history", Long.class);
        return count != null ? count : 0L;
    }
}