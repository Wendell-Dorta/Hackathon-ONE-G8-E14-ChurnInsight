// Explanation: Wrapper adapter that limits concurrency for CPU-bound ONNX inference, and adds caching via Spring CacheManager and metrics via MetricsConfig.
package com.hackathon.databeats.churninsight.infra.adapter.output.inference;

import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.config.MetricsConfig;
import com.hackathon.databeats.churninsight.infra.exception.ModelInferenceException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

public class InferenceExecutorAdapter implements InferencePort {
    private final InferencePort delegate;
    private final ExecutorService executor;
    private final Cache cache;
    private final MetricsConfig metrics;

    public InferenceExecutorAdapter(InferencePort delegate,
                                    ExecutorService executor,
                                    CacheManager cacheManager,
                                    MetricsConfig metrics) {
        this.delegate = Objects.requireNonNull(delegate);
        this.executor = Objects.requireNonNull(executor);
        this.cache = cacheManager != null ? cacheManager.getCache("predictions") : null;
        this.metrics = metrics;
    }

    @Override
    public float[] predict(CustomerProfile profile, Map<String, Object> engineeredFeatures) {
        String userId = profile == null ? null : profile.userId();

        // Try cache
        if (userId != null && cache != null) {
            try {
                Cache.ValueWrapper wrapper = cache.get(userId);
                if (wrapper != null && wrapper.get() instanceof float[] cached) {
                    // cache hit
                    if (metrics != null) metrics.recordCacheHit();
                    return cached.clone();
                }
            } catch (Exception e) {
                // ignore cache failures but log via metrics
                if (metrics != null) metrics.recordError();
            }
        }

        // Submit to bounded executor synchronously
        Callable<float[]> task = () -> {
            long start = System.nanoTime();
            try {
                float[] res = delegate.predict(profile, engineeredFeatures);
                if (metrics != null) metrics.recordPrediction();
                return res;
            } finally {
                long elapsed = System.nanoTime() - start;
                if (metrics != null) metrics.recordLatency(elapsed);
            }
        };

        try {
            Future<float[]> future = executor.submit(task);
            float[] result = future.get();

            // store in cache
            if (userId != null && cache != null && result != null) {
                try { cache.put(userId, result.clone()); } catch (Exception ignore) { if (metrics != null) metrics.recordError(); }
            }

            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ModelInferenceException("Inferência interrompida", e);
        } catch (ExecutionException e) {
            throw new ModelInferenceException("Erro na inferência: " + e.getCause().getMessage(), e.getCause());
        }
    }

    @Override
    public boolean isModelLoaded() { return delegate != null && delegate.isModelLoaded(); }
}
