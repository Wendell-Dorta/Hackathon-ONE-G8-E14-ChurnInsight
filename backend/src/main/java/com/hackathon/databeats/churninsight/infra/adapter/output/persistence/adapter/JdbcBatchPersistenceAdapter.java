package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter;

import com.hackathon.databeats.churninsight.application.port.output.BatchSavePort;
import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter JDBC otimizado para inserção em massa.
 * Usa multi-row INSERT com paralelismo agressivo.
 */
@Slf4j
@Repository("jdbcBatchPersistenceAdapter")
@Primary
public class JdbcBatchPersistenceAdapter implements BatchSavePort {

    private final JdbcTemplate jdbcTemplate;
    private final ExecutorService dbExecutor;
    private final int chunkSize;

    // SQL base - Single row insert for batch update
    private static final String INSERT_SQL =
        "INSERT INTO churn_history (" +
        "id, user_id, gender, age, country, subscription_type, " +
        "listening_time, songs_played_per_day, skip_rate, " +
        "ads_listened_per_week, device_type, offline_listening, " +
        "churn_status, probability, created_at, requester_id, request_ip, " +
        "frustration_index, ad_intensity, songs_per_minute, is_heavy_user, premium_no_offline" +
        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public JdbcBatchPersistenceAdapter(
            JdbcTemplate jdbcTemplate,
            @Value("${app.db.insert-threads:16}") int insertThreads,
            @Value("${app.db.chunk-size:2000}") int chunkSize) {
        this.jdbcTemplate = jdbcTemplate;
        this.chunkSize = chunkSize;
        this.dbExecutor = Executors.newFixedThreadPool(insertThreads);
        log.info("🚀 JdbcBatchPersistenceAdapter: {} threads, chunk size {}", insertThreads, chunkSize);
    }

    @Override
    public void saveAll(List<PredictionHistory> histories) {
        if (histories.isEmpty()) return;

        // Divide em chunks e executa em paralelo máximo
        List<CompletableFuture<Void>> futures = new ArrayList<>((histories.size() / chunkSize) + 1);

        for (int i = 0; i < histories.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, histories.size());
            List<PredictionHistory> chunk = histories.subList(i, end);

            futures.add(CompletableFuture.runAsync(() -> insertMultiRow(chunk), dbExecutor));
        }

        // Aguarda todos em paralelo
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    /**
     * MULTI-ROW INSERT otimizado usando JDBC Batch + RewriteBatchedStatements.
     *
     * Muda estratégia de construir String gigante (que sobrecarrega parser)
     * para usar batching padrão do driver JDBC, que é muito mais eficiente
     * quando rewriteBatchedStatements=true.
     */
    private void insertMultiRow(List<PredictionHistory> histories) {
        if (histories.isEmpty()) return;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try {
             jdbcTemplate.batchUpdate(INSERT_SQL, histories, histories.size(),
                (ps, h) -> {
                    int col = 1;
                    ps.setString(col++, h.id());
                    ps.setString(col++, h.userId());
                    ps.setString(col++, h.gender());
                    ps.setInt(col++, h.age() != null ? h.age() : 0);
                    ps.setString(col++, h.country());
                    ps.setString(col++, h.subscriptionType());
                    ps.setDouble(col++, h.listeningTime() != null ? h.listeningTime() : 0.0);
                    ps.setInt(col++, h.songsPlayedPerDay() != null ? h.songsPlayedPerDay() : 0);
                    ps.setDouble(col++, h.skipRate() != null ? h.skipRate() : 0.0);
                    ps.setInt(col++, h.adsListenedPerWeek() != null ? h.adsListenedPerWeek() : 0);
                    ps.setString(col++, h.deviceType());
                    ps.setBoolean(col++, h.offlineListening() != null && h.offlineListening());
                    ps.setString(col++, h.churnStatus().name());
                    ps.setDouble(col++, h.probability() != null ? h.probability() : 0.0);
                    ps.setTimestamp(col++, h.createdAt() != null ? Timestamp.valueOf(h.createdAt()) : now);
                    ps.setString(col++, h.requesterId());
                    ps.setString(col++, h.requestIp());
                    ps.setObject(col++, h.frustrationIndex());
                    ps.setObject(col++, h.adIntensity());
                    ps.setObject(col++, h.songsPerMinute());
                    ps.setObject(col++, h.isHeavyUser());
                    ps.setObject(col++, h.premiumNoOffline());
                });
        } catch (Exception e) {
             log.error("Erro ao salvar batch de {} registros: {}", histories.size(), e.getMessage());
             throw e;
        }
    }

    @Override
    public void saveBatch(List<PredictionHistory> histories, int batchSize) {
        saveAll(histories);
    }

    @Override
    public long countTotalPredictions() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM churn_history", Long.class);
        return count != null ? count : 0L;
    }
}