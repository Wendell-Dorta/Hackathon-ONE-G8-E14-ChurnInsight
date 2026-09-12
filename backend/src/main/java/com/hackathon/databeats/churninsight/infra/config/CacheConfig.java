package com.hackathon.databeats.churninsight.infra.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Value("${app.cache.ttl-minutes}")
    private int ttlMinutes;

    @Value("${app.cache.max-size}")
    private int maxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        cacheManager.setCaffeine(java.util.Objects.requireNonNull(
            Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(maxSize)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((Object key, Object value, RemovalCause cause) -> {
                    if (log.isDebugEnabled() && cause != RemovalCause.REPLACED) {
                        log.debug("Cache evict: key={} cause={}", key, cause);
                    }
                })
        , "Caffeine builder must not be null"));

        cacheManager.setCacheNames(List.of(
                "stats",
                "predictions",
                "predictionStats"
        ));

        return cacheManager;
    }
}