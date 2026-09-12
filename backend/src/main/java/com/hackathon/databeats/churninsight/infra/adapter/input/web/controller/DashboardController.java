package com.hackathon.databeats.churninsight.infra.adapter.input.web.controller;

import com.hackathon.databeats.churninsight.application.dto.DashboardMetricsResponse;
import com.hackathon.databeats.churninsight.application.service.DashboardMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Métricas consolidadas para o Dashboard")
public class DashboardController {
    private final DashboardMetricsService dashboardMetricsService;

    @GetMapping(value = "/metrics")
    @Operation(
            summary = "Métricas do Dashboard",
            description = "Retorna métricas consolidadas para os cards do dashboard"
    )
    public ResponseEntity<DashboardMetricsResponse> getMetrics() {
        return ResponseEntity.ok(this.dashboardMetricsService.getMetrics());
    }
}