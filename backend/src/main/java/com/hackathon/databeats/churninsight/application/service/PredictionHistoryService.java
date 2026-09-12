package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionHistoryResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.application.port.output.PredictionHistoryQueryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para consulta paginada e agregações de histórico de predições.
 * Delega todo o acesso a dados para {@link PredictionHistoryQueryPort},
 * mantendo a camada de aplicação isolada de detalhes de infraestrutura.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PredictionHistoryService {

    private final PredictionHistoryQueryPort predictionHistoryQueryPort;

    /**
     * Busca paginada com filtros dinâmicos.
     *
     * @param filter Filtros de busca (todos opcionais)
     * @param page Número da página (0-indexed)
     * @param size Tamanho da página (max 100)
     * @param sortBy Campo para ordenação
     * @param sortDir Direção da ordenação (asc/desc)
     * @return Resposta paginada com estatísticas
     */
    public PaginatedResponse<PredictionHistoryResponse> search(
            PredictionSearchFilter filter,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validação e limitação de tamanho
        size = Math.min(size, 100);
        page = Math.max(page, 0);

        return predictionHistoryQueryPort.search(filter, page, size, sortBy, sortDir);
    }

    /**
     * Retorna estatísticas globais para os filtros do frontend.
     */
    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new HashMap<>();

        List<Object[]> genderCounts = predictionHistoryQueryPort.countByGender();
        Map<String, Long> genders = new HashMap<>();
        for (Object[] row : genderCounts) {
            genders.put((String) row[0], (Long) row[1]);
        }
        options.put("genders", genders);

        List<Object[]> subscriptionCounts = predictionHistoryQueryPort.countBySubscriptionType();
        Map<String, Long> subscriptions = new HashMap<>();
        for (Object[] row : subscriptionCounts) {
            subscriptions.put((String) row[0], (Long) row[1]);
        }
        options.put("subscriptionTypes", subscriptions);

        options.put("totalRecords", predictionHistoryQueryPort.count());
        options.put("willChurnCount", predictionHistoryQueryPort.countByChurnStatus(ChurnStatus.WILL_CHURN));
        options.put("willStayCount", predictionHistoryQueryPort.countByChurnStatus(ChurnStatus.WILL_STAY));

        return options;
    }

    /**
     * Retorna estatísticas globais para o dashboard.
     */
    public Map<String, Object> getGlobalStatistics() {
        long total = predictionHistoryQueryPort.count();
        long churners = predictionHistoryQueryPort.countByChurnStatus(ChurnStatus.WILL_CHURN);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_predictions", total);
        stats.put("total_churners", churners);
        stats.put("churn_rate", total > 0 ? (double) churners / total : 0.0);
        return stats;
    }

    /**
     * Busca User IDs para autocomplete.
     */
    public List<String> searchUserIds(String prefix) {
        if (prefix == null || prefix.length() < 2) {
            return List.of();
        }
        return predictionHistoryQueryPort.findUserIdsByPrefix(prefix, 10);
    }

    /**
     * Agrupa métricas necessárias para o dashboard de forma otimizada (DB-side aggregations).
     */
    public Map<String, Object> getAggregates() {
        Map<String, Object> out = new HashMap<>();

        // Buckets de probabilidade
        Object[] buckets = predictionHistoryQueryPort.getProbabilityBuckets();
        if (buckets != null && buckets.length == 4) {
            out.put("probabilityBuckets", new long[]{
                    ((Number) buckets[0]).longValue(),
                    ((Number) buckets[1]).longValue(),
                    ((Number) buckets[2]).longValue(),
                    ((Number) buckets[3]).longValue()
            });
        }

        // Risk factor counts
        Object[] risks = normalizeAggregateTuple(predictionHistoryQueryPort.getRiskFactorCounts(), 5);
        if (risks != null) {
            Map<String, Long> riskMap = new HashMap<>();
            riskMap.put("FREE_HIGH_ADS", ((Number) risks[0]).longValue());
            riskMap.put("HIGH_SKIP_RATE", ((Number) risks[1]).longValue());
            riskMap.put("HIGH_FRUSTRATION", ((Number) risks[2]).longValue());
            riskMap.put("PREMIUM_NO_OFFLINE", ((Number) risks[3]).longValue());
            riskMap.put("LOW_ENGAGEMENT", ((Number) risks[4]).longValue());
            out.put("riskFactorCounts", riskMap);
        }

        // Global stats
        Object[] global = predictionHistoryQueryPort.getGlobalStats();
        if (global != null && global.length >= 4) {
            long total = ((Number) global[0]).longValue();
            double avg = global[1] != null ? ((Number) global[1]).doubleValue() : 0.0;
            long churners = ((Number) global[2]).longValue();
            long stayers = ((Number) global[3]).longValue();
            out.put("total", total);
            out.put("averageProbability", avg);
            out.put("totalChurners", churners);
            out.put("totalStayers", stayers);
            out.put("churnRate", total > 0 ? (double) churners / total : 0.0);
            out.put("revenueAtRisk", churners * 12.90);
        }

        return out;
    }

    private Object[] normalizeAggregateTuple(Object[] raw, int expectedSize) {
        if (raw == null) return null;
        if (raw.length >= expectedSize) return raw;
        if (raw.length == 1 && raw[0] instanceof Object[] nested && nested.length >= expectedSize) {
            return nested;
        }
        return null;
    }
}
