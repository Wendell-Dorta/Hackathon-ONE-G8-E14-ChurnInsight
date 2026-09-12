package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RetentionKpisResponse(
        @JsonProperty("total_actions") long totalActions,
        @JsonProperty("executed_actions") long executedActions,
        @JsonProperty("execution_rate") double executionRate,
        @JsonProperty("total_outcomes") long totalOutcomes,
        @JsonProperty("retained_count") long retainedCount,
        @JsonProperty("retention_rate") double retentionRate,
        @JsonProperty("recovered_revenue") double recoveredRevenue,
        @JsonProperty("priority_high_threshold") double priorityHighThreshold,
        @JsonProperty("priority_medium_threshold") double priorityMediumThreshold
) {
}
