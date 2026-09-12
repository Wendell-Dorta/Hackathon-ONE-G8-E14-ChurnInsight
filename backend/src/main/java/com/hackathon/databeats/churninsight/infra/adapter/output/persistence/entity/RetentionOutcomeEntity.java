package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity;

import com.hackathon.databeats.churninsight.domain.enums.RetentionOutcomeType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "retention_outcome")
@Data
public class RetentionOutcomeEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    @Column(name = "action_id", columnDefinition = "CHAR(36)", nullable = false)
    private String actionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome_type", length = 40, nullable = false)
    private RetentionOutcomeType outcomeType;

    @Column(name = "retained", nullable = false)
    private boolean retained;

    @Column(name = "recovered_revenue", nullable = false)
    private double recoveredRevenue;

    @Column(name = "observed_at", nullable = false)
    private LocalDateTime observedAt = LocalDateTime.now();

    @Column(name = "comment", length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
