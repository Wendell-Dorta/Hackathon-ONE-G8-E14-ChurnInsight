package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.DashboardMetricsResponse;
import com.hackathon.databeats.churninsight.application.dto.DashboardMetricsResponse.FeatureImportanceItem;
import com.hackathon.databeats.churninsight.application.dto.DashboardMetricsResponse.RiskFactorItem;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import com.hackathon.databeats.churninsight.application.port.output.PredictionHistoryQueryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardMetricsService {

    private final PredictionHistoryQueryPort predictionHistoryQueryPort;
    private final ModelMetadataPort modelMetadataPort;

        @Value("${dashboard.include-legacy-churn-distribution:true}")
        private boolean includeLegacyChurnDistribution;

    public DashboardMetricsResponse getMetrics() {
        // 1 - Total de clientes (fonte única: histórico persistido)
        long totalCustomers = Math.max(0L, predictionHistoryQueryPort.count());

        // 2 - Clientes em risco (TOP 25% por probabilidade)  -> regra Mariana
        Long customersAtRisk = predictionHistoryQueryPort.countTop25AtRisk();
        if (customersAtRisk == null || customersAtRisk < 0) customersAtRisk = 0L;

        // 3 - Clientes em monitoramento (%) = TOP 25% (não é WILL_CHURN)
        double monitoringRate = totalCustomers > 0
            ? Math.max(0.0, (customersAtRisk * 100.0) / totalCustomers)
            : 0.0;

        // 4 - Receita em risco (alinhada com o mesmo critério do TOP 25%)
        double revenueAtRisk = Math.max(0.0, this.calculateRevenueAtRisk());

        // 5 - Precisão do modelo (0..1)
        double modelAccuracy = Math.max(0.0, modelMetadataPort.getAcuracia());

        // 6 - Principais fatores (consolidado no backend)
        List<RiskFactorItem> riskFactors = buildRiskFactors(totalCustomers);
        if (riskFactors == null || riskFactors.isEmpty()) {
            riskFactors = List.of(RiskFactorItem.builder()
            .name("Dados insuficientes para análise de risco")
            .count(0L)
            .percentage(0.0)
            .build());
        }

        // 7 - Feature importance (proxy baseado em frequência dos fatores)
        List<FeatureImportanceItem> featureImportance = buildFeatureImportanceFromRiskFactors(riskFactors);
        if (featureImportance == null || featureImportance.isEmpty()) {
            featureImportance = List.of(FeatureImportanceItem.builder()
            .name("Sem fatores de risco detectados")
            .value(1.0)
            .build());
        }

        DashboardMetricsResponse.DashboardMetricsResponseBuilder responseBuilder = DashboardMetricsResponse.builder()
            .totalCustomers(totalCustomers)
            .customersAtRisk(customersAtRisk)
            .globalChurnRate(round(monitoringRate))
            .revenueAtRisk(round2(revenueAtRisk))
            .modelAccuracy(modelAccuracy)
            .riskFactors(riskFactors)
            .featureImportance(featureImportance);

        if (includeLegacyChurnDistribution) {
            responseBuilder.churnDistribution(buildChurnDistribution());
        }

        return responseBuilder.build();
    }

        /**
         * Campo legado para compatibilidade temporaria: [willStay, willChurn]
         */
        private List<Long> buildChurnDistribution() {
                long willChurn = predictionHistoryQueryPort.countByChurnStatus(ChurnStatus.WILL_CHURN);
                long willStay = predictionHistoryQueryPort.countByChurnStatus(ChurnStatus.WILL_STAY);
                return List.of(willStay, willChurn);
        }

    /**
     * Fatores de risco: usa query agregada já existente no repo.
     * Repo retorna (na ordem): free+ads, skipHigh, frustHigh, premiumNoOffline, lowListeningTime
     */
    private List<RiskFactorItem> buildRiskFactors(long totalCustomers) {
        Object[] raw = normalizeAggregateTuple(predictionHistoryQueryPort.getRiskFactorCounts(), 5);
        Map<String, Long> counts = new LinkedHashMap<>();
        if (raw != null) {
            counts.put("Anúncios por Semana", safeLong(raw[0]));
            counts.put("Taxa de Pulos Elevada", safeLong(raw[1]));
            counts.put("Índice de Frustração Alto", safeLong(raw[2]));
            counts.put("Subutilização Premium", safeLong(raw[3]));
            counts.put("Tempo de Escuta Baixo", safeLong(raw[4]));
        }
        long denom = Math.max(1, totalCustomers);
        List<RiskFactorItem> result = counts.entrySet().stream()
            .filter(e -> e.getValue() > 0)
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .map(e -> RiskFactorItem.builder()
                .name(e.getKey())
                .count(e.getValue())
                .percentage(round((e.getValue() * 100.0) / denom))
                .build())
            .toList();
        return result;
    }

    /**
     * Alguns providers/JPA podem devolver a tupla agregada "aninhada" em uma posição.
     * Este helper normaliza para um array linear previsível.
     */
    private Object[] normalizeAggregateTuple(Object[] raw, int expectedSize) {
        if (raw == null) return null;

        if (raw.length >= expectedSize) return raw;

        if (raw.length == 1 && raw[0] instanceof Object[] nested && nested.length >= expectedSize) {
            return nested;
        }

        return null;
    }

    /**
     * Proxy para o gráfico: normaliza pela soma dos fatores (0..1).
     */
    private List<FeatureImportanceItem> buildFeatureImportanceFromRiskFactors(List<RiskFactorItem> riskFactors) {
        if (riskFactors == null || riskFactors.isEmpty()) {
            return List.of(FeatureImportanceItem.builder()
                .name("Sem fatores de risco detectados")
                .value(1.0)
                .build());
        }
        long sum = riskFactors.stream().mapToLong(RiskFactorItem::getCount).sum();
        if (sum <= 0) {
            return List.of(FeatureImportanceItem.builder()
                .name("Sem fatores de risco detectados")
                .value(1.0)
                .build());
        }
        return riskFactors.stream()
            .map(rf -> FeatureImportanceItem.builder()
                .name(rf.getName())
                .value((double) rf.getCount() / (double) sum)
                .build())
            .toList();
    }

    /**
     * Regra simples de receita estimada por tipo de assinatura.
     */
    private double calculateRevenueAtRisk() {
        Map<String, Double> planValues = Map.of(
                "Premium", 23.90,
                "Family", 40.90,
                "Duo", 31.90,
                "Student", 12.90,
                "Free", 0.0
        );

        List<Object[]> top25ByPlan = predictionHistoryQueryPort.getTop25SubscriptionCounts();
        
        if (top25ByPlan == null || top25ByPlan.isEmpty()) {
            return 0.0;
        }

        return top25ByPlan.stream()
                .mapToDouble(row -> {
                    String subscriptionType = row[0] == null ? "" : String.valueOf(row[0]);
                    long count = safeLong(row[1]);
                    double planValue = planValues.getOrDefault(subscriptionType, 0.0);
                    return count * planValue;
                })
                .sum();
    }

    private long safeLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }

    // 1 casa decimal (para % e números gerais)
    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    // 2 casas (pra dinheiro)
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}