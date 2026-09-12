package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.BatchProcessingStatus;
import com.hackathon.databeats.churninsight.application.dto.BatchResult;
import com.hackathon.databeats.churninsight.application.port.input.BatchProcessingUseCase;
import com.hackathon.databeats.churninsight.application.port.output.BatchSavePort;
import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.rules.ChurnBusinessRules;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import com.monitorjbl.xlsx.StreamingReader;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.processor.AbstractRowProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Serviço de processamento em lote ULTRA OTIMIZADO.
 * Target: 10.000 registros/segundo (100K em 10s)
 */
@Service
@Slf4j
public class BatchProcessingService implements BatchProcessingUseCase {

    private final BatchSavePort batchSavePort;
    private final InferencePort inferencePort;
    private final CacheManager cacheManager;
    private final ModelMetadataPort metadata;
    private final TaskExecutor taskExecutor;
    private final int batchSize;
    private final int maxRecords;

    // ForkJoinPool dedicado para inferência - usa TODOS os cores
    private final ForkJoinPool inferencePool;

    private final Map<String, BatchProcessingStatus> jobStatuses = new ConcurrentHashMap<>();

    // Semaphore para controlar backpressure e evitar OOM
    private final Semaphore processingPermits;

    public BatchProcessingService(
            @Qualifier("jdbcBatchPersistenceAdapter") BatchSavePort batchSavePort,
            InferencePort inferencePort,
            CacheManager cacheManager,
            ModelMetadataPort metadata,
            @Qualifier("batchExecutor") TaskExecutor taskExecutor,
            @Value("${app.batch.size:5000}") int batchSize,
            @Value("${app.batch.inference-threads:0}") int inferenceThreads,
            @Value("${app.batch.max-records:100000}") int maxRecords) {
        this.batchSavePort = batchSavePort;
        this.inferencePort = inferencePort;
        this.cacheManager = cacheManager;
        this.metadata = metadata;
        this.taskExecutor = taskExecutor;
        this.batchSize = batchSize;
        this.maxRecords = maxRecords;

        // Se 0, usa todos os processadores disponíveis
        int threads = inferenceThreads > 0 ? inferenceThreads : Runtime.getRuntime().availableProcessors();
        this.inferencePool = new ForkJoinPool(threads);

        // Limita número de batches simultâneos na memória (ex: 8 batches de 5k registros)
        this.processingPermits = new Semaphore(8);

        log.info("🚀 BatchProcessingService TURBO - Batch: {} | Inference Threads: {} | Max Records: {} | CPUs: {}",
                batchSize, threads, maxRecords, Runtime.getRuntime().availableProcessors());
    }

    @Override
    public String startBatchProcessing(MultipartFile multipartFile, String requestIp) {
        String jobId = com.hackathon.databeats.churninsight.infra.util.UUIDv7.randomUUIDString();
        String originalFilename = multipartFile.getOriginalFilename();

        BatchProcessingStatus initialStatus = new BatchProcessingStatus(
                jobId, "INITIALIZING", 0, 0, 0, 0, LocalDateTime.now(), null,
                originalFilename, multipartFile.getSize(), null
        );
        jobStatuses.put(jobId, initialStatus);

        try {
            Path tempDir = Files.createTempDirectory("churn_batch_");
            // Nunca usar o nome original como caminho — evita path traversal
            String safeExtension = (originalFilename != null && originalFilename.toLowerCase().endsWith(".xlsx")) ? "xlsx" : "csv";
            Path tempFile = Files.createTempFile(tempDir, "upload_", "." + safeExtension);
            Files.copy(multipartFile.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("Arquivo salvo temporariamente para processamento: {}", tempFile);

            CompletableFuture.runAsync(() -> {
                try {
                    processFileInternal(tempFile.toFile(), requestIp, jobId, originalFilename);
                } finally {
                    try {
                        Files.deleteIfExists(tempFile);
                        Files.deleteIfExists(tempDir);
                    } catch (IOException e) {
                        log.warn("Não foi possível limpar arquivo temporário: {}", tempFile);
                    }
                }
            }, taskExecutor);

        } catch (IOException e) {
            log.error("Erro ao salvar arquivo temporário", e);
            throw new RuntimeException("Falha ao preparar arquivo para processamento", e);
        }

        return jobId;
    }

    private void processFileInternal(File file, String requestIp, String jobId, String originalFilename) {
        long jobStartTime = System.currentTimeMillis();
        AtomicLong totalDbTime = new AtomicLong(0);
        List<String> errors = new CopyOnWriteArrayList<>();
        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger totalRead = new AtomicInteger(0); // Contador síncrono de leitura

        try {
            updateJobStatus(jobId, "RUNNING", "Processando stream de dados...", 0);
            List<CustomerProfile> profileBuffer = new ArrayList<>(batchSize);

            try (InputStream is = new BufferedInputStream(new FileInputStream(file), 1024 * 1024)) { // 1MB buffer
                if (originalFilename.toLowerCase().endsWith(".csv")) {
                    CsvParserSettings settings = new CsvParserSettings();
                    settings.detectFormatAutomatically();
                    settings.setHeaderExtractionEnabled(true);
                    settings.setSkipEmptyLines(true);
                    settings.setMaxCharsPerColumn(10000);

                    // OTIMIZAÇÃO: Aumentar buffer interno do parser para arquivos grandes
                    settings.setInputBufferSize(1024 * 64); // 64KB buffer interno
                    settings.setReadInputOnSeparateThread(true); // Leitura em thread separada

                    settings.setProcessor(new AbstractRowProcessor() {
                        @Override
                        public void rowProcessed(String[] row, ParsingContext context) {
                            try {
                                if (totalRead.incrementAndGet() > maxRecords) {
                                    throw new RuntimeException("Limite de registros excedido. Máximo permitido: " + maxRecords);
                                }
                                if (context.currentLine() <= 2)
                                    validateCsvHeaders(context);
                                profileBuffer.add(parseRowToProfile(row, context.headers()));

                                if (profileBuffer.size() >= batchSize) {
                                    submitBatchAsync(new ArrayList<>(profileBuffer), requestIp, jobId,
                                            totalDbTime, processedCount, successCount, futures);
                                    profileBuffer.clear();
                                }
                            } catch (Exception e) {
                                if (errors.size() < 50)
                                    errors.add("Linha " + context.currentLine() + ": " + e.getMessage());
                            }
                        }
                    });
                    new CsvParser(settings).parse(is);

                } else if (originalFilename.toLowerCase().endsWith(".xlsx")) {
                    try (Workbook workbook = StreamingReader.builder().rowCacheSize(100).bufferSize(4096).open(is)) {
                        Sheet sheet = workbook.getSheetAt(0);
                        Iterator<Row> rowIterator = sheet.iterator();
                        String[] headers = null;

                        while (rowIterator.hasNext()) {
                            Row row = rowIterator.next();
                            if (totalRead.incrementAndGet() > maxRecords) {
                                throw new RuntimeException("Limite de registros excedido. Máximo permitido: " + maxRecords);
                            }
                            if (row.getRowNum() == 0) {
                                headers = extractHeadersFromRow(row);
                                validateExcelHeaders(headers);
                                continue;
                            }
                            try {
                                profileBuffer.add(parseExcelRowToProfile(row, headers));
                                if (profileBuffer.size() >= batchSize) {
                                    submitBatchAsync(new ArrayList<>(profileBuffer), requestIp, jobId,
                                            totalDbTime, processedCount, successCount, futures);
                                    profileBuffer.clear();
                                }
                            } catch (Exception e) {
                                if (errors.size() < 50)
                                    errors.add("Linha Excel " + (row.getRowNum() + 1) + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }

            if (!profileBuffer.isEmpty()) {
                submitBatchAsync(new ArrayList<>(profileBuffer), requestIp, jobId,
                        totalDbTime, processedCount, successCount, futures);
            }

            // Aguarda todos os batches terminarem
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            long totalDuration = System.currentTimeMillis() - jobStartTime;
            log.info("📊 JOB {} FINALIZADO - Total: {}ms | DB: {}ms | Média: {} reg/s",
                    jobId, totalDuration, totalDbTime.get(), (processedCount.get() * 1000) / Math.max(totalDuration, 1));

            updateJobStatus(jobId, new BatchResult(jobId, errors.isEmpty(), processedCount.get(),
                    successCount.get(), errors.size(), LocalDateTime.now(), totalDuration, errors, "Processamento concluído"));

        } catch (Exception e) {
            log.error("Erro fatal no job {}", jobId, e);
            handleJobError(jobId, e);
        }
    }

    private void submitBatchAsync(List<CustomerProfile> batch, String requestIp, String jobId,
                                  AtomicLong totalDbTime, AtomicInteger processedCount,
                                  AtomicInteger successCount, List<CompletableFuture<Void>> futures) {
        try {
            processingPermits.acquire(); // Bloqueia se atingir limite de batches simultâneos

            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return processAndSaveBatch(batch, requestIp, totalDbTime);
                } finally {
                    processingPermits.release();
                }
            }, taskExecutor).thenAccept(count -> {
                successCount.addAndGet(count);
                processedCount.addAndGet(batch.size());
                updateProcessedRecords(jobId, processedCount.get());
            });

            futures.add(future);

            // Limpa futures concluídos para economizar memória na lista
            futures.removeIf(CompletableFuture::isDone);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrompido ao aguardar permissão de processamento", e);
        }
    }

    /**
     * Processa batch com máximo paralelismo usando ForkJoinPool dedicado.
     * Pipeline: Inferência paralela → Salvar assíncrono no DB
     */
    private int processAndSaveBatch(List<CustomerProfile> profiles, String requestIp, AtomicLong dbTimeAccumulator) {
        // Pré-calcula valores constantes (evita chamadas repetidas)
        final LocalDateTime batchTimestamp = LocalDateTime.now();
        final double threshold = metadata.getThresholdOtimo();
        final long timestampMillis = System.currentTimeMillis();

        try {
            // Usa ForkJoinPool dedicado para inferência (não compete com outras tarefas)
            List<PredictionHistory> histories = inferencePool.submit(() ->
                    profiles.parallelStream()
                            .map(p -> createHistoryFromProfile(p, threshold, timestampMillis, batchTimestamp, requestIp))
                            .filter(Objects::nonNull)
                            .toList()
            ).get();

            if (!histories.isEmpty()) {
                long startDb = System.currentTimeMillis();
                batchSavePort.saveAll(histories);
                dbTimeAccumulator.addAndGet(System.currentTimeMillis() - startDb);
            }
            return histories.size();

        } catch (Exception e) {
            log.error("Erro no processamento do batch: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Cria instância de domínio PredictionHistory a partir do perfil do cliente.
     */
    private PredictionHistory createHistoryFromProfile(
            CustomerProfile p, double threshold, long timestampMillis,
            LocalDateTime batchTimestamp, String requestIp) {
        try {
            // 1. Features de negócio
            Map<String, Object> features = ChurnBusinessRules.calculateEngineeredFeatures(p);

            // 2. Inferência
            float[] prediction = inferencePort.predict(p, features);

            if (Math.random() < 0.001) {
                log.info("DEBUG probs userId={} p0={} p1={} threshold={}",
                        p.userId(), prediction[0], prediction[1], threshold);
            }

            double prob = prediction[1];
            ChurnStatus status = prob >= threshold ? ChurnStatus.WILL_CHURN : ChurnStatus.WILL_STAY;

            return PredictionHistory.builder()
                    .id(com.hackathon.databeats.churninsight.infra.util.UUIDv7.generateString(timestampMillis))
                    .userId(p.userId())
                    .age(p.age())
                    .gender(p.gender())
                    .country(p.country())
                    .subscriptionType(p.subscriptionType())
                    .deviceType(p.deviceType())
                    .listeningTime(p.listeningTime())
                    .songsPlayedPerDay(p.songsPlayedPerDay())
                    .skipRate(p.skipRate())
                    .adsListenedPerWeek(p.adsListenedPerWeek())
                    .offlineListening(p.offlineListening())
                    .churnStatus(status)
                    .probability(prob)
                    .createdAt(batchTimestamp)
                    .requesterId("batch-file")
                    .requestIp(requestIp)
                    .frustrationIndex((Double) features.get("frustration_index"))
                    .adIntensity((Double) features.get("ad_intensity"))
                    .songsPerMinute((Double) features.get("songs_per_minute"))
                    .isHeavyUser((Boolean) features.get("is_heavy_user"))
                    .premiumNoOffline((Boolean) features.get("premium_no_offline"))
                    .build();
        } catch (Exception ex) {
            log.warn("Falha ao processar userId={} motivo={}", p.userId(), ex.getMessage());
            return null;
        }
    }

    // --- MÉTODOS AUXILIARES ---

    private void validateCsvHeaders(ParsingContext context) {
        validateHeadersGeneric(context.headers(), "CSV");
    }

    private void validateExcelHeaders(String[] headers) {
        validateHeadersGeneric(headers, "Excel");
    }

    private void validateHeadersGeneric(String[] headers, String type) {
        List<String> req = Arrays.asList("user_id", "gender", "age", "country", "subscription_type", "listening_time", "songs_played_per_day", "skip_rate", "ads_listened_per_week", "device_type", "offline_listening");
        Set<String> fileHeaders = Arrays.stream(headers).map(h -> h.toLowerCase().trim().replace(" ", "_")).collect(Collectors.toSet());
        List<String> missing = req.stream().filter(r -> !fileHeaders.contains(r)).toList();
        if (!missing.isEmpty())
            throw new IllegalArgumentException("Colunas faltando no " + type + ": " + String.join(", ", missing));
    }

    private CustomerProfile mapToProfile(Map<String, String> data) {
        return CustomerProfile.builder()
                .userId(data.getOrDefault("user_id", data.getOrDefault("userid", "")))
                .gender(normalizeGender(data.getOrDefault("gender", "")))
                .age(parseInt(data.get("age")))
                .country(normalizeCountry(data.getOrDefault("country", "")))
                .subscriptionType(normalizeSubscription(data.getOrDefault("subscription_type", data.getOrDefault("subscriptiontype", ""))))
                .listeningTime(parseDouble(data.getOrDefault("listening_time", data.get("listeningtime"))))
                .songsPlayedPerDay(parseInt(data.getOrDefault("songs_played_per_day", data.get("songsplayedperday"))))
                .skipRate(parseDouble(data.getOrDefault("skip_rate", data.get("skiprate"))))
                .adsListenedPerWeek(parseInt(data.getOrDefault("ads_listened_per_week", data.get("adslistenedperweek"))))
                .deviceType(normalizeDevice(data.getOrDefault("device_type", data.getOrDefault("devicetype", ""))))
                .offlineListening(parseBoolean(data.getOrDefault("offline_listening", data.get("offlinelistening"))))
                .build();
    }

    private String normalizeGender(String v) {
        if (v == null) return "";
        v = v.trim().toLowerCase();
        if (v.equals("male") || v.equals("m") || v.equals("masculino")) return "Masculino";
        if (v.equals("female") || v.equals("f") || v.equals("feminino")) return "Feminino";
        return v; // fallback
    }

    private String normalizeCountry(String v) {
        if (v == null) return "";
        v = v.trim().toUpperCase();
        // Se seu modelo espera PT-BR (Brasil/França/Índia), converta aqui:
        if (v.equals("BR") || v.equals("BRAZIL") || v.equals("BRASIL")) return "Brasil";
        if (v.equals("FR") || v.equals("FRANCE") || v.equals("FRANÇA")) return "França";
        if (v.equals("IN") || v.equals("INDIA") || v.equals("ÍNDIA")) return "Índia";
        return v;
    }

    private String normalizeDevice(String v) {
        if (v == null)
            return "";
        v = v.trim().toLowerCase();
        if (v.equals("desktop"))
            return "Web";
        if (v.equals("mobile"))
            return "Mobile";
        return v;
    }

    private String normalizeSubscription(String v) {
        if (v == null)
            return "";
        v = v.trim();
        // mantém como está, mas garante capitalização conhecida:
        if (v.equalsIgnoreCase("free"))
            return "Free";
        if (v.equalsIgnoreCase("premium"))
            return "Premium";
        if (v.equalsIgnoreCase("student"))
            return "Student";
        if (v.equalsIgnoreCase("family"))
            return "Family";
        if (v.equalsIgnoreCase("duo"))
            return "Duo";
        return v;
    }

    private CustomerProfile parseRowToProfile(String[] row, String[] headers) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < Math.min(row.length, headers.length); i++)
            map.put(headers[i].toLowerCase().trim().replace(" ", "_"), row[i]);
        return mapToProfile(map);
    }

    private CustomerProfile parseExcelRowToProfile(Row row, String[] headers) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < headers.length; i++) {
            var cell = row.getCell(i);
            map.put(headers[i].toLowerCase().trim().replace(" ", "_"), cell != null ? cell.toString() : "");
        }
        return mapToProfile(map);
    }

    private int parseInt(String v) {
        try {
            return (int) Double.parseDouble(v);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String v) {
        try {
            return Double.parseDouble(v);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean parseBoolean(String v) {
        if (v == null)
            return false;
        v = v.trim().toLowerCase();
        return v.equals("1") || v.equals("1.0") || v.equals("true") || v.equals("yes") || v.equals("y") || v.equals("sim");
    }


    private String[] extractHeadersFromRow(Row row) {
        int cols = row.getLastCellNum();
        String[] h = new String[cols];
        for (int i = 0; i < cols; i++) {
            var c = row.getCell(i);
            h[i] = c != null ? c.getStringCellValue() : "";
        }
        return h;
    }

    private void updateJobStatus(String id, BatchResult r) {
        BatchProcessingStatus c = jobStatuses.get(id);
        if (c != null)
            jobStatuses.put(id, new BatchProcessingStatus(id, r.success() ? "COMPLETED" : "FAILED", c.totalRecords(), r.totalProcessed(), r.successCount(), r.errorCount(), c.startTime(), LocalDateTime.now(), c.filename(), c.fileSizeBytes(), r.errors().toString()));
    }

    private void updateJobStatus(String id, String s, String m, int p) {
        BatchProcessingStatus c = jobStatuses.get(id);
        if (c != null)
            jobStatuses.put(id, new BatchProcessingStatus(id, s, c.totalRecords(), p, c.successCount(), c.errorCount(), c.startTime(), c.endTime(), c.filename(), c.fileSizeBytes(), m));
    }

    private void updateProcessedRecords(String id, int count) {
        BatchProcessingStatus c = jobStatuses.get(id);
        if (c != null)
            jobStatuses.put(id, new BatchProcessingStatus(id, c.status(), c.totalRecords(), count, c.successCount(), c.errorCount(), c.startTime(), c.endTime(), c.filename(), c.fileSizeBytes(), c.errorMessage()));
    }

    private void handleJobError(String id, Throwable ex) {
        BatchProcessingStatus c = jobStatuses.get(id);
        if (c != null)
            jobStatuses.put(id, new BatchProcessingStatus(id, "FAILED", c.totalRecords(), c.processedRecords(), c.successCount(), c.errorCount(), c.startTime(), LocalDateTime.now(), c.filename(), c.fileSizeBytes(), ex.getMessage()));
    }

    @Override
    public Map<String, Object> getJobStatus(String id) {
        return convertStatusToMap(jobStatuses.get(id));
    }

    @Override
    public boolean isModelHealthy() {
        return inferencePort.isModelLoaded();
    }

    @Override
    public void clearPredictionCache() {
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(n -> {
                var cache = cacheManager.getCache(Objects.requireNonNull(n));
                if (cache != null)
                    cache.clear();
            });
        }
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        return new HashMap<>();
    }

    /**
     * Limpa jobs finalizados há mais de 24 horas para evitar memory leak.
     * Executa a cada hora (3600000ms).
     */
    @Scheduled(fixedRate = 3600000)
    public void cleanupOldJobs() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        int removedCount = 0;

        var iterator = jobStatuses.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BatchProcessingStatus status = entry.getValue();
            if (status.endTime() != null && status.endTime().isBefore(cutoff)) {
                iterator.remove();
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("🧹 Limpeza automática: {} jobs antigos removidos da memória", removedCount);
        }
    }

    private Map<String, Object> convertStatusToMap(BatchProcessingStatus s) {
        if (s == null)
            return Map.of();
        Map<String, Object> m = new HashMap<>();
        m.put("job_id", s.jobId());
        m.put("status", s.status());
        m.put("processed", s.processedRecords());
        m.put("success_count", s.successCount());
        m.put("error_count", s.errorCount());
        m.put("message", s.errorMessage());
        return m;
    }

    @Override
    public CompletableFuture<BatchResult> processCsvFileAsync(MultipartFile file, String requestIp) {
        startBatchProcessing(file, requestIp);
        return CompletableFuture.completedFuture(null);
    }
}

