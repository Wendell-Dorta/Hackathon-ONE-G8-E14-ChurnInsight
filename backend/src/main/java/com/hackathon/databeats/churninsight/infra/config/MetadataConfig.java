package com.hackathon.databeats.churninsight.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.model.thresholds")
@Getter
@Setter
public class MetadataConfig {
    private double maxSkipRate = 2.0;
    private double minListeningTime = 6.0;
    private int minSongsPlayed = 10;
    private double highRiskBaseProbability = 0.70;
}