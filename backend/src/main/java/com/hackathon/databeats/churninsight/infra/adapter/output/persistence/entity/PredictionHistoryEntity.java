package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;

@Entity
@Table(name = "churn_history")
@Data
public class PredictionHistoryEntity implements Persistable<String> {
    @Id
    @Column(columnDefinition = "CHAR(36)", nullable = false)
    private String id;

    // DADOS DE ENTRADA
    @Column(length = 20)
    private String gender;

    private int age;

    @Column(length = 10)
    private String country;

    @Column(name = "subscription_type", length = 30)
    private String subscriptionType;

    @Column(name = "listening_time")
    private double listeningTime;

    @Column(name = "songs_played_per_day")
    private int songsPlayedPerDay;

    @Column(name = "ads_listened_per_week")
    private int adsListenedPerWeek;

    @Column(name = "skip_rate")
    private double skipRate;

    @Column(name = "device_type", length = 30)
    private String deviceType;

    @Column(name = "offline_listening")
    private boolean offlineListening;

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    // SAÍDA DO MODELO
    @Enumerated(EnumType.STRING) // garante que será salvo como texto no banco
    @Column(name = "churn_status", nullable = false, length = 30)
    private ChurnStatus churnStatus;

    @Column(nullable = false)
    private double probability;

    // AUDITORIA
    @Column(name = "requester_id", columnDefinition = "CHAR(36)")
    private String requesterId;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private boolean isNew = true;

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Column(name = "frustration_index")
    private Double frustrationIndex;

    @Column(name = "ad_intensity")
    private Double adIntensity;

    @Column(name = "songs_per_minute")
    private Double songsPerMinute;

    @Column(name = "is_heavy_user")
    private Boolean isHeavyUser;

    @Column(name = "premium_no_offline")
    private Boolean premiumNoOffline;
}