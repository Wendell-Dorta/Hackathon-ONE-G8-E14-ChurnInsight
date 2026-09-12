package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.port.output.PredictionHistoryQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PredictionHistoryServiceTest {

    @Mock
    private PredictionHistoryQueryPort predictionHistoryQueryPort;

    private PredictionHistoryService predictionHistoryService;

    @BeforeEach
    void setUp() {
        predictionHistoryService = new PredictionHistoryService(predictionHistoryQueryPort);
    }

    @Test
    @DisplayName("Deve montar riskFactorCounts com tupla linear")
    void shouldBuildRiskFactorCountsFromLinearTuple() {
        when(predictionHistoryQueryPort.getRiskFactorCounts()).thenReturn(new Object[]{11L, 22L, 33L, 44L, 55L});

        Map<String, Object> aggregates = predictionHistoryService.getAggregates();

        assertNotNull(aggregates);
        assertTrue(aggregates.containsKey("riskFactorCounts"));

        @SuppressWarnings("unchecked")
        Map<String, Long> riskMap = (Map<String, Long>) aggregates.get("riskFactorCounts");
        assertEquals(11L, riskMap.get("FREE_HIGH_ADS"));
        assertEquals(22L, riskMap.get("HIGH_SKIP_RATE"));
        assertEquals(33L, riskMap.get("HIGH_FRUSTRATION"));
        assertEquals(44L, riskMap.get("PREMIUM_NO_OFFLINE"));
        assertEquals(55L, riskMap.get("LOW_ENGAGEMENT"));
    }

    @Test
    @DisplayName("Deve montar riskFactorCounts com tupla aninhada")
    void shouldBuildRiskFactorCountsFromNestedTuple() {
        Object[] nested = new Object[]{9L, 8L, 7L, 6L, 5L};
        when(predictionHistoryQueryPort.getRiskFactorCounts()).thenReturn(new Object[]{nested});

        Map<String, Object> aggregates = predictionHistoryService.getAggregates();

        assertNotNull(aggregates);
        assertTrue(aggregates.containsKey("riskFactorCounts"));

        @SuppressWarnings("unchecked")
        Map<String, Long> riskMap = (Map<String, Long>) aggregates.get("riskFactorCounts");
        assertEquals(9L, riskMap.get("FREE_HIGH_ADS"));
        assertEquals(8L, riskMap.get("HIGH_SKIP_RATE"));
        assertEquals(7L, riskMap.get("HIGH_FRUSTRATION"));
        assertEquals(6L, riskMap.get("PREMIUM_NO_OFFLINE"));
        assertEquals(5L, riskMap.get("LOW_ENGAGEMENT"));
    }
}
