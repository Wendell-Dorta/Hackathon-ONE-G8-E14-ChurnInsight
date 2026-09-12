package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.RetentionActionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RetentionActionRepository extends JpaRepository<RetentionActionEntity, String> {

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByStatusAndCreatedAtBetween(RetentionActionStatus status, LocalDateTime from, LocalDateTime to);

    @Query("""
        SELECT COALESCE(SUM(o.recoveredRevenue), 0)
        FROM RetentionOutcomeEntity o
        JOIN RetentionActionEntity a ON a.id = o.actionId
        WHERE a.createdAt BETWEEN :from AND :to
    """)
    Double sumRecoveredRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
        SELECT o.retained, COUNT(o)
        FROM RetentionOutcomeEntity o
        JOIN RetentionActionEntity a ON a.id = o.actionId
        WHERE a.createdAt BETWEEN :from AND :to
        GROUP BY o.retained
    """)
    List<Object[]> getRetentionOutcomeCounts(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
        SELECT
            h.id,
            h.user_id,
            h.probability,
            h.subscription_type,
            h.churn_status,
            h.created_at,
            ranked.row_num,
            ranked.total_count,
            la.id as action_id,
            la.status as action_status
        FROM (
            SELECT id,
                   ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
                   COUNT(*) OVER () as total_count
            FROM churn_history
        ) ranked
        JOIN churn_history h ON h.id = ranked.id
        LEFT JOIN (
            SELECT ra.client_id, ra.id, ra.status
            FROM retention_action ra
            INNER JOIN (
                SELECT client_id, MAX(created_at) AS max_created
                FROM retention_action
                GROUP BY client_id
            ) last_ra ON last_ra.client_id = ra.client_id AND last_ra.max_created = ra.created_at
        ) la ON la.client_id = h.id
        WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
        ORDER BY h.probability DESC
    """, countQuery = "SELECT CEIL(COUNT(*) * 0.25) FROM churn_history", nativeQuery = true)
    Page<Object[]> getPrioritizedTop25(Pageable pageable);
}
