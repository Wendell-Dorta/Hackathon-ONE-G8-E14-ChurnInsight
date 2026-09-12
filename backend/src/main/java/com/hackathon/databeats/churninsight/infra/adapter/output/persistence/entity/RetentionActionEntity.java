package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity;

import com.hackathon.databeats.churninsight.domain.enums.RetentionActionStatus;
import com.hackathon.databeats.churninsight.domain.enums.RetentionActionType;
import com.hackathon.databeats.churninsight.domain.enums.RetentionChannel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "retention_action")
@Data
public class RetentionActionEntity {

    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    @Column(name = "client_id", columnDefinition = "CHAR(36)", nullable = false)
    private String clientId;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 40, nullable = false)
    private RetentionActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", length = 30, nullable = false)
    private RetentionChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private RetentionActionStatus status;

    @Column(name = "owner", length = 80)
    private String owner;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
