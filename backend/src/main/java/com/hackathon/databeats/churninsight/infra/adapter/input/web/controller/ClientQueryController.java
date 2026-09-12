package com.hackathon.databeats.churninsight.infra.adapter.input.web.controller;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionHistoryResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase;
import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase.ClientStatistics;
import com.hackathon.databeats.churninsight.application.service.PredictionHistoryService;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller Unificado para Consulta de Clientes.
 *
 * <p>Combina as funcionalidades de busca histórica (filtros avançados) com
 * as consultas rápidas de predição (counts e stats otimizados).</p>
 */
@RestController
@RequestMapping(value = "/clients", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Client Query API", description = "Endpoints unificados para gestão e análise de clientes")
@Slf4j
@RequiredArgsConstructor
public class ClientQueryController {

    // Serviço 1: Acesso rápido via SQL direto (Count, Stats, Risco)
    private final ClientPredictionQueryUseCase predictionUseCase;

    // Serviço 2: Acesso histórico com filtros complexos (Search, Autocomplete)
    private final PredictionHistoryService historyService;

    // =================================================================================
    // 1. BUSCA MESTRA (Substitui o getAllClients simples)
    // =================================================================================

    @GetMapping
    @Operation(summary = "Buscar clientes com filtros",
            description = "Endpoint principal de listagem. Suporta paginação, ordenação e múltiplos filtros combinados.")
    public ResponseEntity<PaginatedResponse<PredictionHistoryResponse>> searchClients(
            @Parameter(description = "Número da página (0-indexed)")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Direção da ordenação (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDir,

            // === FILTROS (Vindos do ClientHistoryController) ===
            @Parameter(description = "Status de churn")
            @RequestParam(required = false) ChurnStatus status,

            @Parameter(description = "Probabilidade mínima")
            @RequestParam(required = false) Double minProbability,

            @Parameter(description = "Probabilidade máxima")
            @RequestParam(required = false) Double maxProbability,

            @Parameter(description = "Gênero")
            @RequestParam(required = false) String gender,

            @Parameter(description = "Idade mínima")
            @RequestParam(required = false) Integer minAge,

            @Parameter(description = "Idade máxima")
            @RequestParam(required = false) Integer maxAge,

            @Parameter(description = "País")
            @RequestParam(required = false) String country,

            @Parameter(description = "Tipo de assinatura")
            @RequestParam(required = false) String subscriptionType,

            @Parameter(description = "Tipo de dispositivo")
            @RequestParam(required = false) String deviceType,

            @Parameter(description = "Data inicial (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Data final (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(description = "Apenas Heavy Users")
            @RequestParam(required = false) Boolean isHeavyUser,

            @Parameter(description = "Usa Offline")
            @RequestParam(required = false) Boolean offlineListening,

            @Parameter(description = "Frustração mínima")
            @RequestParam(required = false) Double minFrustration,

            @Parameter(description = "Frustração máxima")
            @RequestParam(required = false) Double maxFrustration,

            @Parameter(description = "Busca textual por ID")
            @RequestParam(required = false) String userId
    ) {
        log.debug("🔍 Busca avançada: page={}, size={}, userId={}, status={}", page, size, userId, status);

        PredictionSearchFilter filter = PredictionSearchFilter.builder()
                .churnStatus(status)
                .minProbability(minProbability)
                .maxProbability(maxProbability)
                .gender(gender)
                .minAge(minAge)
                .maxAge(maxAge)
                .country(country)
                .subscriptionType(subscriptionType)
                .deviceType(deviceType)
                .startDate(startDate)
                .endDate(endDate)
                .isHeavyUser(isHeavyUser)
                .offlineListening(offlineListening)
                .minFrustrationIndex(minFrustration)
                .maxFrustrationIndex(maxFrustration)
                .userId(userId)
                .build();

        return ResponseEntity.ok(historyService.search(filter, page, size, sortBy, sortDir));
    }

    // =================================================================================
    // 2. ENDPOINTS DE DASHBOARD & KPI (Do antigo ClientPredictionController)
    // =================================================================================

    @GetMapping("/count")
    @Operation(summary = "Contar total de clientes", description = "Retorna o número absoluto de registros no banco para os Cards do Dashboard")
    public ResponseEntity<Map<String, Long>> getTotalClients() {
        long total = predictionUseCase.getTotalClients();
        return ResponseEntity.ok(Map.of("total", total, "count", total));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Estatísticas de Churn (SQL)", description = "Retorna agregação (AVG, MAX, MIN, COUNT) calculada via SQL otimizado")
    public ResponseEntity<ClientStatistics> getStatistics() {
        return ResponseEntity.ok(predictionUseCase.getStatistics());
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Detalhe do Cliente", description = "Busca dados completos de um cliente específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não existe")
    })
    public ResponseEntity<ClientPrediction> getClientPrediction(@PathVariable String clientId) {
        return predictionUseCase.findByClientId(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =================================================================================
    // 3. ENDPOINTS DE RISCO & SEGMENTAÇÃO (Do antigo ClientPredictionController)
    // =================================================================================

    @GetMapping("/high-risk")
    @Operation(summary = "Listar Alto Risco", description = "Lista clientes com probabilidade acima do threshold (Paginado)")
    public ResponseEntity<List<ClientPrediction>> getHighRiskClients(
            @RequestParam(defaultValue = "0.7") double threshold,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("⚠️ Buscando alto risco (th={}) [p={}, s={}]", threshold, page, size);
        return ResponseEntity.ok(predictionUseCase.findHighRiskClients(threshold, page, size));
    }

    @GetMapping("/will-churn")
    @Operation(summary = "Listar Prestes a Cancelar", description = "Lista todos os clientes com status WILL_CHURN (Paginado)")
    public ResponseEntity<List<ClientPrediction>> getClientsWhoWillChurn(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        log.info("📉 Buscando churners confirmados [p={}, s={}]", page, size);
        return ResponseEntity.ok(predictionUseCase.findClientsWhoWillChurn(page, size));
    }

    // =================================================================================
    // 4. ENDPOINTS AUXILIARES DE UI (Do antigo ClientHistoryController)
    // =================================================================================

    @GetMapping("/filter-options")
    @Operation(summary = "Opções de Filtros", description = "Retorna valores distintos (países, planos, etc) para popular dropdowns no Frontend")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        return ResponseEntity.ok(historyService.getFilterOptions());
    }

    @GetMapping("/autocomplete/user-id")
    @Operation(summary = "Autocomplete User ID", description = "Sugere User IDs baseado no input parcial")
    public ResponseEntity<List<String>> autocompleteUserId(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String prefix) {
        String effective = (prefix != null && !prefix.isBlank()) ? prefix : query;
        return ResponseEntity.ok(historyService.searchUserIds(effective));
    }

    @GetMapping("/by-status/{status}")
    @Operation(summary = "Buscar por status (path)", description = "Compatibilidade para frontend que usa rota por path")
    public ResponseEntity<PaginatedResponse<PredictionHistoryResponse>> byStatus(
            @PathVariable ChurnStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PredictionSearchFilter filter = PredictionSearchFilter.builder().churnStatus(status).build();
        PaginatedResponse<PredictionHistoryResponse> response = historyService.search(filter, page, size, "createdAt", "desc");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/aggregates")
    @Operation(summary = "Aggregates para dashboard", description = "Retorna métricas pre-agrupadas para alimentar o dashboard")
    public ResponseEntity<Map<String, Object>> getAggregates() {
        return ResponseEntity.ok(historyService.getAggregates());
    }
}