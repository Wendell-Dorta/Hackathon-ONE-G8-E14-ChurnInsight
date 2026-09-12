package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.application.dto.BatchResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Port de entrada que define o caso de uso para processamento em lote.
 *
 * <p>Responsável por orquestrar a leitura, validação, predição e persistência
 * de múltiplos perfis de clientes fornecidos em arquivo CSV ou XLSX.</p>
 *
 * <p><b>Ciclo de vida:</b></p>
 * <ol>
 *   <li>Cliente inicia job via {@link #startBatchProcessing}</li>
 *   <li>Sistema carrega e valida arquivo em background</li>
 *   <li>Cliente consulta progresso via {@link #getJobStatus}</li>
 *   <li>Resultados são persistidos incrementalmente</li>
 * </ol>
 *
 * <p><b>Performance:</b> Otimizado para processar até 10k registros/segundo
 * usando paralelismo de CPU e JDBC batch insert.</p>
 *
 * <p><b>Implementações:</b></p>
 * <ul>
 *   <li>{@link com.hackathon.databeats.churninsight.application.service.BatchProcessingService}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface BatchProcessingUseCase {

	/**
	 * Inicia processamento assíncrono de um arquivo CSV ou XLSX.
	 *
	 * <p>Retorna imediatamente com um ID de job para rastreamento.
	 * O processamento executa em background thread pool dedicado.</p>
	 *
	 * @param file arquivo contendo perfis de clientes (CSV ou XLSX)
	 * @param requestIp endereço IP do requisitante (para auditoria)
	 * @return ID único do job para consulta de status posterior
	 * @throws IllegalArgumentException se arquivo inválido ou muito grande (>200MB)
	 */
	String startBatchProcessing(MultipartFile file, String requestIp);

	/**
	 * Obtém o status atual de um job de processamento.
	 *
	 * @param jobId identificador do job (retornado por startBatchProcessing)
	 * @return mapa contendo status, progresso, métricas e timestamps
	 */
	Map<String, Object> getJobStatus(String jobId);

	/**
	 * Verifica se o modelo ONNX está carregado e funcional.
	 *
	 * <p>Útil para health checks antes de iniciar processamento em lote.</p>
	 *
	 * @return {@code true} se modelo está pronto para inferência
	 */
	boolean isModelHealthy();

	/**
	 * Limpa todo o cache de predições.
	 *
	 * <p>Requer permissão de administrador. Força recálculo em próximas requisições.</p>
	 *
	 * @throws SecurityException se usuário não tem role ADMIN
	 */
	void clearPredictionCache();

	/**
	 * Obtém estatísticas de cache para monitoramento.
	 *
	 * @return mapa com hits, misses, tamanho e outras métricas
	 */
	Map<String, Object> getCacheStatistics();

	/**
	 * Processa arquivo de forma assíncrona com notificação futura.
	 *
	 * <p>Alternativa a startBatchProcessing para casos que necessitam
	 * de notificação ao fim do processamento.</p>
	 *
	 * @param file arquivo para processar
	 * @param requestIp endereço IP do requisitante
	 * @return {@link CompletableFuture} que completa com resultado do batch
	 */
	CompletableFuture<BatchResult> processCsvFileAsync(MultipartFile file, String requestIp);
}