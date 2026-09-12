package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRetentionActionStatusRequest(
        @JsonAlias({"status", "action_status"})
        @NotNull RetentionActionStatus status
) {
}
