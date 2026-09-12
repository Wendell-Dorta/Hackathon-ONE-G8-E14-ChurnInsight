package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RetentionPriorityItemResponse(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("probability") double probability,
        @JsonProperty("churn_status") ChurnStatus churnStatus,
        @JsonProperty("subscription_type") String subscriptionType,
        @JsonProperty("expected_monthly_value") double expectedMonthlyValue,
        @JsonProperty("recovery_probability") double recoveryProbability,
        @JsonProperty("rank_score") double rankScore,
        @JsonProperty("risk_signal") double riskSignal,
        @JsonProperty("recency_factor") double recencyFactor,
        @JsonProperty("priority_score") double priorityScore,
        @JsonProperty("suggested_action") RetentionActionType suggestedAction,
        @JsonProperty("action_id") String actionId,
        @JsonProperty("action_status") RetentionActionStatus actionStatus,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
}
