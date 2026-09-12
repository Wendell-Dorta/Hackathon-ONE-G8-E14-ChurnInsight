package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionType;
import com.hackathon.databeats.churninsight.domain.enums.RetentionChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateRetentionActionRequest(
        @JsonAlias({"clientId", "client_id"}) @NotBlank String clientId,
        @JsonAlias({"actionType", "action_type"}) @NotNull RetentionActionType actionType,
        @NotNull RetentionChannel channel,
        String owner,
        @JsonAlias({"scheduledAt", "scheduled_at"}) LocalDateTime scheduledAt,
        String notes,
        RetentionActionStatus status
) {
}
