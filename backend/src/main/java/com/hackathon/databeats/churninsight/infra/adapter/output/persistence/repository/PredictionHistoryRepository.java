package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório de histórico de predições.
 * Extende JpaSpecificationExecutor para queries dinâmicas com filtros.
 */
@Repository
public interface PredictionHistoryRepository extends
        JpaRepository<PredictionHistoryEntity, String>,
        JpaSpecificationExecutor<PredictionHistoryEntity> {

    /**
     * Conta predições por status (usa índice)
     */
    long countByChurnStatus(ChurnStatus status);

    /**
     * Busca paginada por status (usa índice)
     */
    Page<PredictionHistoryEntity> findByChurnStatus(ChurnStatus status, Pageable pageable);

    /**
     * Busca paginada por intervalo de data (usa índice em created_at)
     */
    Page<PredictionHistoryEntity> findByCreatedAtBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Busca paginada por probabilidade
     */
    List<PredictionHistoryEntity> findByProbabilityGreaterThanEqual(double probability, Pageable pageable);

    /**
     * Estatísticas agregadas (COUNT, AVG) - query otimizada
     */
    @Query("SELECT COUNT(p), AVG(p.probability), " +
           "SUM(CASE WHEN p.churnStatus = 'WILL_CHURN' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN p.churnStatus = 'WILL_STAY' THEN 1 ELSE 0 END) " +
           "FROM PredictionHistoryEntity p")
    Object[] getGlobalStats();

    /**
     * Contagem rápida por gênero (para filtros do frontend)
     */
    @Query("SELECT p.gender, COUNT(p) FROM PredictionHistoryEntity p GROUP BY p.gender")
    List<Object[]> countByGender();

    /**
     * Contagem rápida por tipo de assinatura
     */
    @Query("SELECT p.subscriptionType, COUNT(p) FROM PredictionHistoryEntity p GROUP BY p.subscriptionType")
    List<Object[]> countBySubscriptionType();

    /**
     * Busca por userId com LIKE (para autocomplete)
     */
    @Query("SELECT DISTINCT p.userId FROM PredictionHistoryEntity p WHERE p.userId LIKE :prefix% ORDER BY p.userId")
    java.util.List<String> findUserIdsByPrefix(@Param("prefix") String prefix, Pageable pageable);

    /**
     * Agregação: contagem por faixas de probabilidade
     */
    @Query("SELECT " +
            "SUM(CASE WHEN p.probability < 0.2 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.probability >= 0.2 AND p.probability < 0.5 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.probability >= 0.5 AND p.probability < 0.7 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.probability >= 0.7 THEN 1 ELSE 0 END) " +
            "FROM PredictionHistoryEntity p")
    Object[] getProbabilityBuckets();

    /**
     * Agregação: contagens por fatores de risco heurísticos
     */
    @Query("SELECT " +
            "SUM(CASE WHEN p.subscriptionType = 'Free' AND p.adsListenedPerWeek > 15 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.skipRate > 0.4 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.frustrationIndex IS NOT NULL AND p.frustrationIndex > 3.0 THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.premiumNoOffline = true THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN p.listeningTime < 100 THEN 1 ELSE 0 END) " +
            "FROM PredictionHistoryEntity p")
    Object[] getRiskFactorCounts();

    /**
     * Conta quantos clientes ficam no TOP 25% (probability >= cutoff_top25).
     */
    @Query(value = """
    SELECT COUNT(*)
    FROM (
      SELECT id,
             ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
             COUNT(*) OVER () as total_count
      FROM churn_history
    ) ranked
    WHERE row_num <= CEIL(total_count * 0.25)
    """, nativeQuery = true)
    Long countTop25AtRisk();

                /**
                 * Agrega clientes do TOP 25% por probabilidade agrupando por tipo de assinatura.
                 * Retorna pares [subscription_type, count].
                 */
                @Query(value = """
                SELECT ranked.subscription_type, COUNT(*) as count
                FROM (
                        SELECT subscription_type,
                                                 ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
                                                 COUNT(*) OVER () as total_count
                        FROM churn_history
                ) ranked
                WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
                GROUP BY ranked.subscription_type
                ORDER BY count DESC
                """, nativeQuery = true)
                List<Object[]> getTop25SubscriptionCounts();
}