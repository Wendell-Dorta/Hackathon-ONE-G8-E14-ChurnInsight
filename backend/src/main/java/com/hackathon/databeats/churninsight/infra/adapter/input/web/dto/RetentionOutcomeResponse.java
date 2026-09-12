package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.databeats.churninsight.domain.enums.RetentionOutcomeType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RetentionOutcomeResponse(
        @JsonProperty("id") String id,
        @JsonProperty("action_id") String actionId,
        @JsonProperty("outcome_type") RetentionOutcomeType outcomeType,
        @JsonProperty("retained") boolean retained,
        @JsonProperty("recovered_revenue") double recoveredRevenue,
        @JsonProperty("observed_at") LocalDateTime observedAt,
        @JsonProperty("comment") String comment
) {
}
