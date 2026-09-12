package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.DashboardMetricsResponse;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import com.hackathon.databeats.churninsight.application.port.output.PredictionHistoryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardMetricsServiceTest {

    @Mock
    private PredictionHistoryQueryPort predictionHistoryQueryPort;

    @Mock
    private ModelMetadataPort modelMetadataPort;

    private DashboardMetricsService dashboardMetricsService;

    @BeforeEach
    void setUp() {
        dashboardMetricsService = new DashboardMetricsService(predictionHistoryQueryPort, modelMetadataPort);
    }

    @Test
    @DisplayName("Deve preencher risk_factors quando agregação vier em tupla linear")
    void shouldBuildRiskFactorsFromLinearAggregateTuple() {
        stubCommonMetrics();
        when(predictionHistoryQueryPort.getRiskFactorCounts())
                .thenReturn(new Object[]{10L, 20L, 30L, 40L, 5L});

        DashboardMetricsResponse response = dashboardMetricsService.getMetrics();

        assertNotNull(response);
        assertNotNull(response.getRiskFactors());
        assertFalse(response.getRiskFactors().isEmpty());
        assertEquals(5, response.getRiskFactors().size());
        assertEquals("Subutilização Premium", response.getRiskFactors().get(0).getName());
        assertEquals(40L, response.getRiskFactors().get(0).getCount());
    }

    @Test
    @DisplayName("Deve preencher risk_factors quando agregação vier em tupla aninhada")
    void shouldBuildRiskFactorsFromNestedAggregateTuple() {
        stubCommonMetrics();
        Object[] nested = new Object[]{8L, 16L, 24L, 32L, 4L};
        when(predictionHistoryQueryPort.getRiskFactorCounts())
                .thenReturn(new Object[]{nested});

        DashboardMetricsResponse response = dashboardMetricsService.getMetrics();

        assertNotNull(response);
        assertNotNull(response.getRiskFactors());
        assertFalse(response.getRiskFactors().isEmpty());
        assertEquals(5, response.getRiskFactors().size());
        assertEquals("Subutilização Premium", response.getRiskFactors().get(0).getName());
        assertEquals(32L, response.getRiskFactors().get(0).getCount());

        List<DashboardMetricsResponse.FeatureImportanceItem> featureImportance = response.getFeatureImportance();
        assertNotNull(featureImportance);
        assertFalse(featureImportance.isEmpty());
    }

    private void stubCommonMetrics() {
        when(predictionHistoryQueryPort.count()).thenReturn(100L);
        when(predictionHistoryQueryPort.countTop25AtRisk()).thenReturn(25L);
        List<Object[]> top25SubscriptionCounts = new ArrayList<>();
        top25SubscriptionCounts.add(new Object[]{"Premium", 10L});
        when(predictionHistoryQueryPort.getTop25SubscriptionCounts())
            .thenReturn(top25SubscriptionCounts);
        when(modelMetadataPort.getAcuracia()).thenReturn(0.6488);
    }
}
