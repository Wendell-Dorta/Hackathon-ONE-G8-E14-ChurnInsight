package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.hackathon.databeats.churninsight.domain.enums.RetentionOutcomeType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateRetentionOutcomeRequest(
        @JsonAlias({"outcomeType", "outcome_type"}) @NotNull RetentionOutcomeType outcomeType,
        @NotNull Boolean retained,
        @JsonAlias({"recoveredRevenue", "recovered_revenue"}) Double recoveredRevenue,
        @JsonAlias({"observedAt", "observed_at"}) LocalDateTime observedAt,
        String comment
) {
}
