package com.hackathon.databeats.churninsight.infra.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.databeats.churninsight.application.dto.ApiContract;
import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.application.service.ChurnPredictionService;
import com.hackathon.databeats.churninsight.infra.adapter.output.inference.InferenceExecutorAdapter;
import com.hackathon.databeats.churninsight.infra.adapter.output.inference.OnnxRuntimeAdapter;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Configuração de beans da aplicação.
 *
 * <p>Responsável pela criação e injeção de dependências seguindo a Arquitetura Hexagonal.
 * Os adapters são criados aqui e expostos através de suas interfaces (ports).</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Slf4j
@Configuration
public class BeanConfiguration {

    @Value("${app.batch.inference-threads:2}")
    private int inferenceThreads;

    /**
     * Cria um ExecutorService para gerenciar threads de inferência.
     *
     * @return instância de ExecutorService configurada com número de threads definido
     */
    @Bean
    public ExecutorService inferenceExecutor() {
        int threads = Math.max(1, inferenceThreads);
        final java.util.concurrent.atomic.AtomicInteger idx = new java.util.concurrent.atomic.AtomicInteger(0);
        ThreadFactory tf = runnable -> {
            Thread t = new Thread(runnable, "inference-worker-" + idx.getAndIncrement());
            t.setDaemon(true);
            return t;
        };
        return Executors.newFixedThreadPool(threads, tf);
    }

    /**
     * Carrega os metadados do modelo de ML do arquivo JSON.
     *
     * @return instância de ModelMetadata com configurações do modelo
     * @throws IOException se o arquivo não for encontrado ou estiver corrompido
     */
    @Bean
    public ModelMetadata modelMetadata() throws IOException {
        try (InputStream is = new ClassPathResource("metadata.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            ModelMetadata metadata = mapper.readValue(is, ModelMetadata.class);
            log.info("Metadados do modelo carregados - Nome: {} v{} | Threshold: {}",
                    metadata.getName(), metadata.getVersion(), metadata.getThresholdOtimo());
            return metadata;
        }
    }

    /**
     * Cria o adapter ONNX para inferência do modelo de ML, envolvendo-o em um adaptador de execução
     * para gerenciamento de threads e métricas.
     *
     * @param metadata metadados do modelo para configuração
     * @param inferenceExecutor executor para gerenciamento de threads de inferência
     * @param cacheManager gerenciador de cache para otimização de desempenho
     * @param metricsConfig configuração de métricas para monitoramento
     * @return implementação de InferencePort usando ONNX Runtime e adaptador de execução
     * @throws Exception se o modelo não puder ser carregado
     */
    @Bean
    public InferencePort inferencePort(ModelMetadata metadata,
                                       ExecutorService inferenceExecutor,
                                       CacheManager cacheManager,
                                       MetricsConfig metricsConfig) throws Exception {
        try (InputStream modelStream = new ClassPathResource("modelo_hackathon.onnx").getInputStream()) {
            log.info("Carregando modelo ONNX para inferência...");
            OnnxRuntimeAdapter adapter = new OnnxRuntimeAdapter(modelStream, metadata);
            return new InferenceExecutorAdapter(adapter, inferenceExecutor, cacheManager, metricsConfig);
        }
    }

    /**
     * Cria o serviço de predição de churn.
     *
     * @param saveHistoryPort port para persistência de histórico
     * @param inferencePort port para inferência do modelo
     * @param modelMetadataPort port para metadados do modelo
     * @return serviço de predição configurado
     */
    @Bean
    public ChurnPredictionService churnPredictionService(
            SaveHistoryPort saveHistoryPort,
            InferencePort inferencePort,
            ModelMetadataPort modelMetadataPort) {
        return new ChurnPredictionService(saveHistoryPort, inferencePort, modelMetadataPort);
    }

    /**
     * Carrega a lista de predições pré-calculadas para demonstração.
     * Este bean é carregado somente no profile 'demo' para não confundir dados reais em produção.
     *
     * @return lista de clientes com predições geradas pelo pipeline de ML
     * @throws IOException se o arquivo não for encontrado
     */
    @Bean
    @Profile("demo")
    public List<ClientPrediction> clientPredictions() throws IOException {
        try (InputStream is = new ClassPathResource("clients.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            List<ClientPrediction> clients = mapper.readValue(is, new TypeReference<>() {});
            log.info("Carregados {} clientes do arquivo clients.json (profile=demo)", clients.size());
            return clients;
        }
    }

    /**
     * Carrega o contrato da API definido pelo time de Data Science.
     *
     * @return contrato com estrutura de entrada/saída esperada
     * @throws IOException se o arquivo não for encontrado
     */
    @Bean
    public ApiContract apiContract() throws IOException {
        try (InputStream is = new ClassPathResource("contrato_api.json").getInputStream()) {
            ObjectMapper mapper = new ObjectMapper();
            ApiContract contract = mapper.readValue(is, ApiContract.class);
            log.info("Contrato da API carregado - Endpoint: {} | Modelo: {} v{}",
                    contract.getEndpoint(),
                    contract.getMetadata().getModelName(),
                    contract.getMetadata().getModelVersion());
            return contract;
        }
    }
}