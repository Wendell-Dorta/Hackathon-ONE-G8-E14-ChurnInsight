package com.hackathon.databeats.churninsight.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ForkJoinPool;

/**
 * Configuração de executores assíncronos otimizada para VM com 12 threads e 8GB RAM.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${app.batch.core-pool-size:6}")
    private int batchCorePoolSize;

    @Value("${app.batch.max-pool-size:12}")
    private int batchMaxPoolSize;

    /**
     * Executor otimizado para processamento de arquivos CSV/XLSX em batch.
     * Configurado para maximizar uso dos 12 threads disponíveis.
     */
    @Bean("batchExecutor")
    public TaskExecutor batchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(batchCorePoolSize);
        executor.setMaxPoolSize(batchMaxPoolSize);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Batch-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Executor para tarefas assíncronas leves.
     */
    @Bean("taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

    /**
     * Configura o ForkJoinPool comum para usar todos os threads disponíveis.
     * Isso otimiza parallelStream() usado no batch processing.
     */
    @Bean
    public ForkJoinPool forkJoinPoolConfig() {
        int parallelism = Runtime.getRuntime().availableProcessors();
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(parallelism));
        return ForkJoinPool.commonPool();
    }
}