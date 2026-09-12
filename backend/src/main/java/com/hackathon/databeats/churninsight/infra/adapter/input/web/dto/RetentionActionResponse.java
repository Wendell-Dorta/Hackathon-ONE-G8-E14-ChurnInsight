package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionType;
import com.hackathon.databeats.churninsight.domain.enums.RetentionChannel;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RetentionActionResponse(
        @JsonProperty("id") String id,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("user_id") String userId,
        @JsonProperty("action_type") RetentionActionType actionType,
        @JsonProperty("channel") RetentionChannel channel,
        @JsonProperty("status") RetentionActionStatus status,
        @JsonProperty("owner") String owner,
        @JsonProperty("scheduled_at") LocalDateTime scheduledAt,
        @JsonProperty("executed_at") LocalDateTime executedAt,
        @JsonProperty("notes") String notes,
        @JsonProperty("created_at") LocalDateTime createdAt
) {
}
