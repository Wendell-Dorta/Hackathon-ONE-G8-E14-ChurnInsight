package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionType;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.dto.*;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.RetentionActionEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.RetentionOutcomeEntity;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.PredictionHistoryRepository;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.RetentionActionRepository;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.RetentionOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RetentionOperationsService {

    private final RetentionActionRepository retentionActionRepository;
    private final RetentionOutcomeRepository retentionOutcomeRepository;
    private final PredictionHistoryRepository predictionHistoryRepository;

    @Value("${retention.priority.high-threshold:6.0}")
    private double priorityHighThreshold;

    @Value("${retention.priority.medium-threshold:3.0}")
    private double priorityMediumThreshold;

    public PaginatedResponse<RetentionPriorityItemResponse> getPrioritized(int page, int size) {
        Page<Object[]> raw = retentionActionRepository.getPrioritizedTop25(PageRequest.of(page, size));

        List<RetentionPriorityItemResponse> items = raw.getContent().stream()
                .map(this::toPriorityItem)
                .toList();

        return PaginatedResponse.<RetentionPriorityItemResponse>builder()
                .content(items)
                .page(raw.getNumber())
                .size(raw.getSize())
                .totalElements(raw.getTotalElements())
                .totalPages(raw.getTotalPages())
                .first(raw.isFirst())
                .last(raw.isLast())
                .hasNext(raw.hasNext())
                .hasPrevious(raw.hasPrevious())
                .build();
    }

    public RetentionActionResponse createAction(CreateRetentionActionRequest request) {
        String clientId = Objects.requireNonNull(request.clientId(), "clientId is required");
        var client = predictionHistoryRepository.findById(clientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        RetentionActionEntity entity = new RetentionActionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setClientId(clientId);
        entity.setUserId(client.getUserId());
        entity.setActionType(request.actionType());
        entity.setChannel(request.channel());
        entity.setOwner(request.owner());
        entity.setScheduledAt(request.scheduledAt());
        entity.setNotes(request.notes());
        entity.setStatus(request.status() == null ? RetentionActionStatus.PLANNED : request.status());

        RetentionActionEntity saved = retentionActionRepository.save(entity);
        return toActionResponse(saved);
    }

    public RetentionActionResponse updateActionStatus(String actionId, UpdateRetentionActionStatusRequest request) {
        String safeActionId = Objects.requireNonNull(actionId, "actionId is required");
        RetentionActionEntity entity = retentionActionRepository.findById(safeActionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acao de retencao nao encontrada"));

        entity.setStatus(request.status());
        if (request.status() == RetentionActionStatus.EXECUTED) {
            entity.setExecutedAt(LocalDateTime.now());
        }

        RetentionActionEntity saved = retentionActionRepository.save(entity);
        return toActionResponse(saved);
    }

    public RetentionOutcomeResponse registerOutcome(String actionId, CreateRetentionOutcomeRequest request) {
        String safeActionId = Objects.requireNonNull(actionId, "actionId is required");
        RetentionActionEntity action = retentionActionRepository.findById(safeActionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Acao de retencao nao encontrada"));

        RetentionOutcomeEntity entity = retentionOutcomeRepository.findByActionId(safeActionId)
                .orElseGet(RetentionOutcomeEntity::new);

        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID().toString());
            entity.setActionId(safeActionId);
        }

        entity.setOutcomeType(request.outcomeType());
        entity.setRetained(Boolean.TRUE.equals(request.retained()));
        entity.setRecoveredRevenue(request.recoveredRevenue() == null ? 0.0 : request.recoveredRevenue());
        entity.setObservedAt(request.observedAt() == null ? LocalDateTime.now() : request.observedAt());
        entity.setComment(request.comment());

        if (action.getStatus() != RetentionActionStatus.EXECUTED) {
            action.setStatus(RetentionActionStatus.EXECUTED);
            action.setExecutedAt(LocalDateTime.now());
            retentionActionRepository.save(action);
        }

        RetentionOutcomeEntity saved = retentionOutcomeRepository.save(entity);
        return RetentionOutcomeResponse.builder()
                .id(saved.getId())
                .actionId(saved.getActionId())
                .outcomeType(saved.getOutcomeType())
                .retained(saved.isRetained())
                .recoveredRevenue(round2(saved.getRecoveredRevenue()))
                .observedAt(saved.getObservedAt())
                .comment(saved.getComment())
                .build();
    }

    public RetentionKpisResponse getKpis(LocalDate from, LocalDate to) {
        LocalDateTime fromDate = (from == null ? LocalDate.now().minusDays(30) : from).atStartOfDay();
        LocalDateTime toDate = (to == null ? LocalDate.now() : to).atTime(23, 59, 59);

        long totalActions = retentionActionRepository.countByCreatedAtBetween(fromDate, toDate);
        long executedActions = retentionActionRepository.countByStatusAndCreatedAtBetween(RetentionActionStatus.EXECUTED, fromDate, toDate);

        List<Object[]> outcomes = retentionActionRepository.getRetentionOutcomeCounts(fromDate, toDate);
        long retainedCount = 0;
        long totalOutcomes = 0;
        for (Object[] row : outcomes) {
            boolean retained = Boolean.TRUE.equals(row[0]);
            long count = row[1] instanceof Number n ? n.longValue() : 0L;
            totalOutcomes += count;
            if (retained) retainedCount += count;
        }

        double recoveredRevenue = retentionActionRepository.sumRecoveredRevenueBetween(fromDate, toDate) == null
                ? 0.0
                : retentionActionRepository.sumRecoveredRevenueBetween(fromDate, toDate);

        double executionRate = totalActions > 0 ? (executedActions * 100.0) / totalActions : 0.0;
        double retentionRate = totalOutcomes > 0 ? (retainedCount * 100.0) / totalOutcomes : 0.0;

        return RetentionKpisResponse.builder()
                .totalActions(totalActions)
                .executedActions(executedActions)
                .executionRate(round1(executionRate))
                .totalOutcomes(totalOutcomes)
                .retainedCount(retainedCount)
                .retentionRate(round1(retentionRate))
                .recoveredRevenue(round2(recoveredRevenue))
            .priorityHighThreshold(round2(priorityHighThreshold))
            .priorityMediumThreshold(round2(priorityMediumThreshold))
                .build();
    }

    private RetentionPriorityItemResponse toPriorityItem(Object[] row) {
        String clientId = String.valueOf(row[0]);
        String userId = row[1] == null ? null : String.valueOf(row[1]);
        double probability = toDouble(row[2]);
        String subscriptionType = row[3] == null ? "Free" : String.valueOf(row[3]);
        ChurnStatus churnStatus = ChurnStatus.valueOf(String.valueOf(row[4]));
        LocalDateTime createdAt = row[5] instanceof java.sql.Timestamp ts ? ts.toLocalDateTime() : null;
        long rowNum = row[6] instanceof Number n ? n.longValue() : 1L;
        long totalCount = row[7] instanceof Number n ? n.longValue() : 1L;
        String actionId = row[8] == null ? null : String.valueOf(row[8]);
        RetentionActionStatus actionStatus = row[9] == null ? null : RetentionActionStatus.valueOf(String.valueOf(row[9]));

        double expectedValue = monthlyValueByPlan().getOrDefault(subscriptionType, 0.0);
        double recoveryProbability = estimateRecoveryProbability(subscriptionType, probability);
        double rankScore = calculateRankScore(rowNum, totalCount);
        double riskSignal = calculateRiskSignal(probability, rankScore);
        double recencyFactor = calculateRecencyFactor(createdAt);
        double priorityScore = expectedValue * recoveryProbability * riskSignal * recencyFactor;
        RetentionActionType suggestedAction = suggestAction(probability, subscriptionType);

        return RetentionPriorityItemResponse.builder()
                .clientId(clientId)
                .userId(userId)
                .probability(round4(probability))
                .churnStatus(churnStatus)
                .subscriptionType(subscriptionType)
                .expectedMonthlyValue(round2(expectedValue))
                .recoveryProbability(round4(recoveryProbability))
                .rankScore(round4(rankScore))
                .riskSignal(round4(riskSignal))
                .recencyFactor(round4(recencyFactor))
                .priorityScore(round4(priorityScore))
                .suggestedAction(suggestedAction)
                .actionId(actionId)
                .actionStatus(actionStatus)
                .createdAt(createdAt)
                .build();
    }

    private RetentionActionResponse toActionResponse(RetentionActionEntity e) {
        return RetentionActionResponse.builder()
                .id(e.getId())
                .clientId(e.getClientId())
                .userId(e.getUserId())
                .actionType(e.getActionType())
                .channel(e.getChannel())
                .status(e.getStatus())
                .owner(e.getOwner())
                .scheduledAt(e.getScheduledAt())
                .executedAt(e.getExecutedAt())
                .notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private Map<String, Double> monthlyValueByPlan() {
        return Map.of(
                "Premium", 23.90,
                "Family", 40.90,
                "Duo", 31.90,
                "Student", 12.90,
                "Free", 0.0
        );
    }

    private double estimateRecoveryProbability(String subscriptionType, double churnProbability) {
        double base = switch (subscriptionType) {
            case "Family", "Duo", "Premium" -> 0.55;
            case "Student" -> 0.48;
            default -> 0.35;
        };

        // Quanto maior o risco, menor a chance de recuperar sem intervenção forte.
        double adjusted = base - (churnProbability * 0.25);
        return Math.max(0.10, Math.min(0.90, adjusted));
    }

    private RetentionActionType suggestAction(double churnProbability, String subscriptionType) {
        if (churnProbability >= 0.80) return RetentionActionType.PERSONAL_CONTACT;
        if ("Free".equalsIgnoreCase(subscriptionType)) return RetentionActionType.PREMIUM_TRIAL;
        if (churnProbability >= 0.65) return RetentionActionType.DISCOUNT_OFFER;
        return RetentionActionType.CONTENT_RECOMMENDATION;
    }

    private double calculateRankScore(long rowNum, long totalCount) {
        if (totalCount <= 1) return 1.0;
        double normalized = 1.0 - ((double) (Math.max(rowNum, 1L) - 1L) / (double) (totalCount - 1L));
        return Math.max(0.0, Math.min(1.0, normalized));
    }

    private double calculateRiskSignal(double probability, double rankScore) {
        // 65% probabilidade + 35% posicionamento relativo no TOP 25%
        double blended = (probability * 0.65) + (rankScore * 0.35);
        return Math.max(0.05, Math.min(1.0, blended));
    }

    private double calculateRecencyFactor(LocalDateTime createdAt) {
        if (createdAt == null) return 0.90;

        long days = Math.max(0L, Duration.between(createdAt, LocalDateTime.now()).toDays());
        if (days <= 7) return 1.00;
        if (days <= 30) return 0.95;
        if (days <= 90) return 0.88;
        return 0.80;
    }

    private double toDouble(Object v) {
        if (v == null) return 0.0;
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); } catch (Exception e) { return 0.0; }
    }

    private double round1(double value) { return Math.round(value * 10.0) / 10.0; }
    private double round2(double value) { return Math.round(value * 100.0) / 100.0; }
    private double round4(double value) { return Math.round(value * 10000.0) / 10000.0; }
}
