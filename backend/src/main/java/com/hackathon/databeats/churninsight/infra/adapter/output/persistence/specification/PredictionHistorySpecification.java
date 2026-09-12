package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.specification;

import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.PredictionHistoryEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Specification para construção de queries dinâmicas de busca de predições.
 * Usa índices do banco de dados para performance otimizada.
 */
public class PredictionHistorySpecification {

    private PredictionHistorySpecification() {
        // Utility class
    }

    /**
     * Constrói uma Specification baseada nos filtros fornecidos.
     * Apenas filtros não-nulos são aplicados.
     */
    public static Specification<PredictionHistoryEntity> withFilters(PredictionSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // === FILTROS DE STATUS ===
            if (filter.churnStatus() != null) {
                predicates.add(cb.equal(root.get("churnStatus"), filter.churnStatus()));
            }

            // === FILTROS DE PROBABILIDADE ===
            if (filter.minProbability() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("probability"), filter.minProbability()));
            }
            if (filter.maxProbability() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("probability"), filter.maxProbability()));
            }

            // === FILTROS DEMOGRÁFICOS ===
            if (filter.gender() != null && !filter.gender().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("gender")), filter.gender().toLowerCase()));
            }
            if (filter.minAge() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("age"), filter.minAge()));
            }
            if (filter.maxAge() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("age"), filter.maxAge()));
            }
            if (filter.country() != null && !filter.country().isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("country")), filter.country().toUpperCase()));
            }
            if (filter.subscriptionType() != null && !filter.subscriptionType().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("subscriptionType")), filter.subscriptionType().toLowerCase()));
            }
            if (filter.deviceType() != null && !filter.deviceType().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("deviceType")), filter.deviceType().toLowerCase()));
            }

            // === FILTROS DE DATA ===
            if (filter.startDate() != null) {
                LocalDateTime startDateTime = filter.startDate().atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            if (filter.endDate() != null) {
                LocalDateTime endDateTime = filter.endDate().atTime(LocalTime.MAX);
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            // === FILTROS DE COMPORTAMENTO ===
            if (filter.isHeavyUser() != null) {
                predicates.add(cb.equal(root.get("isHeavyUser"), filter.isHeavyUser()));
            }
            if (filter.offlineListening() != null) {
                predicates.add(cb.equal(root.get("offlineListening"), filter.offlineListening()));
            }

            // === FILTROS DE FEATURES CALCULADAS ===
            if (filter.minFrustrationIndex() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("frustrationIndex"), filter.minFrustrationIndex()));
            }
            if (filter.maxFrustrationIndex() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("frustrationIndex"), filter.maxFrustrationIndex()));
            }

            // === BUSCA POR TEXTO (userId) ===
            if (filter.userId() != null && !filter.userId().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("userId")),
                        "%" + filter.userId().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Specification para busca por status apenas (mais performática)
     */
    public static Specification<PredictionHistoryEntity> byStatus(ChurnStatus status) {
        return (root, query, cb) -> cb.equal(root.get("churnStatus"), status);
    }

    /**
     * Specification para busca por intervalo de probabilidade
     */
    public static Specification<PredictionHistoryEntity> byProbabilityRange(double min, double max) {
        return (root, query, cb) -> cb.between(root.get("probability"), min, max);
    }

    /**
     * Specification para clientes de alto risco (probability > 0.7)
     */
    public static Specification<PredictionHistoryEntity> highRisk() {
        return (root, query, cb) -> cb.greaterThan(root.get("probability"), 0.7);
    }
}

