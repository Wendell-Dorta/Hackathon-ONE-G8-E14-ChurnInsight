package com.hackathon.databeats.churninsight.infra.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.hackathon.databeats.churninsight.infra.config.MetricsConfig;
import com.hackathon.databeats.churninsight.infra.exception.ApiErrorResponse;
import com.hackathon.databeats.churninsight.infra.util.NetworkUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final Cache<String, Bucket> buckets;
    private final MetricsConfig metricsConfig;
    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.requests-per-second:50}")
    private int requestsPerSecond;

    @Value("${app.rate-limit.burst-capacity:100}")
    private int burstCapacity;

    public RateLimitingFilter(MetricsConfig metricsConfig, ObjectMapper objectMapper) {
        this.metricsConfig = metricsConfig;
        this.objectMapper = objectMapper;
        this.buckets = Caffeine.newBuilder()
                .maximumSize(100_000)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    protected void doFilterInternal(
            @org.checkerframework.checker.nullness.qual.NonNull HttpServletRequest request,
            @org.checkerframework.checker.nullness.qual.NonNull HttpServletResponse response,
            @org.checkerframework.checker.nullness.qual.NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        metricsConfig.incrementActiveRequests();
        try {
            String rateLimitKey = resolveRateLimitKey(request);

            Bucket bucket = buckets.get(rateLimitKey, key -> createBucket());

            long availableTokens = bucket.getAvailableTokens();
            response.setHeader("X-Rate-Limit-Limit", String.valueOf(burstCapacity));
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(availableTokens));

            if (bucket.tryConsume(1)) {
                filterChain.doFilter(request, response);
            } else {
                handleRateLimitExceeded(response, rateLimitKey);
            }
        } finally {
            metricsConfig.decrementActiveRequests();
        }
    }

    private String resolveRateLimitKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "USER:" + auth.getName();
        }

        return "IP:" + NetworkUtils.getClientIp(request);
    }

    private void handleRateLimitExceeded(HttpServletResponse response, String key) throws IOException {
        log.warn("⛔ Rate limit excedido (429) para chave: {}", key);
        metricsConfig.recordError();

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse error = new ApiErrorResponse(
                java.time.Instant.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                "Muitas requisições. Tente novamente em alguns segundos.",
                "/predict"
        );

        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(burstCapacity)
                .refillGreedy(requestsPerSecond, Duration.ofSeconds(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isExcludedPath(String path) {
        return path.startsWith("/actuator") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/favicon.ico") ||
                // Exclude batch prediction endpoints to avoid rate-limiting large file uploads
                path.startsWith("/predict/batch");
    }
}