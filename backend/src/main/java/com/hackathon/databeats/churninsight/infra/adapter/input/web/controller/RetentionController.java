package com.hackathon.databeats.churninsight.infra.adapter.input.web.controller;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.service.RetentionOperationsService;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping(value = "/retention", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Retention", description = "Operacoes de retencao e priorizacao")
public class RetentionController {

    private final RetentionOperationsService retentionOperationsService;

    @GetMapping("/prioritized")
    @Operation(summary = "Lista clientes priorizados", description = "Retorna TOP 25% por risco com score financeiro de prioridade")
    public ResponseEntity<PaginatedResponse<RetentionPriorityItemResponse>> prioritized(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(retentionOperationsService.getPrioritized(page, size));
    }

    @PostMapping(value = "/actions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Cria acao de retencao")
    public ResponseEntity<RetentionActionResponse> createAction(
            @Valid @RequestBody CreateRetentionActionRequest request
    ) {
        return ResponseEntity.ok(retentionOperationsService.createAction(request));
    }

    @PatchMapping(value = "/actions/{actionId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Atualiza status da acao")
    public ResponseEntity<RetentionActionResponse> updateStatus(
            @PathVariable String actionId,
            @Valid @RequestBody UpdateRetentionActionStatusRequest request
    ) {
        return ResponseEntity.ok(retentionOperationsService.updateActionStatus(actionId, request));
    }

    @PostMapping(value = "/actions/{actionId}/outcome", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Registra outcome da acao")
    public ResponseEntity<RetentionOutcomeResponse> outcome(
            @PathVariable String actionId,
            @Valid @RequestBody CreateRetentionOutcomeRequest request
    ) {
        return ResponseEntity.ok(retentionOperationsService.registerOutcome(actionId, request));
    }

    @GetMapping("/kpis")
    @Operation(summary = "KPIs de operacao de retencao")
    public ResponseEntity<RetentionKpisResponse> kpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(retentionOperationsService.getKpis(from, to));
    }
}
