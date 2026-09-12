package com.hackathon.databeats.churninsight.application.dto;

import java.time.LocalDateTime;

/**
 * DTO (record) que representa o status atual de um job de processamento em lote.
 *
 * <p>Transmitido em tempo real para permitir que clientes monitorem o progresso
 * de processamento de arquivos com múltiplas predições.</p>
 *
 * <p><b>Ciclo de vida do status:</b></p>
 * <ul>
 *   <li>INITIALIZING: Arquivo sendo carregado e validado</li>
 *   <li>PROCESSING: Predições em execução</li>
 *   <li>COMPLETED: Processamento finalizado com sucesso</li>
 *   <li>FAILED: Erro crítico durante processamento</li>
 * </ul>
 *
 * @param jobId identificador único do job (UUIDv7)
 * @param status estado atual do processamento (recomenda-se usar enum em próximas versões)
 * @param totalRecords quantidade total de registros a processar
 * @param processedRecords quantidade de registros já processados
 * @param successCount quantidade de predições bem-sucedidas
 * @param errorCount quantidade de predições que falharam
 * @param startTime momento de início do processamento
 * @param endTime momento de finalização (null se em progresso)
 * @param filename nome do arquivo enviado
 * @param fileSizeBytes tamanho do arquivo em bytes
 * @param errorMessage mensagem de erro (null se sem erro)
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record BatchProcessingStatus(
        String jobId,
        String status,
        int totalRecords,
        int processedRecords,
        int successCount,
        int errorCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String filename,
        long fileSizeBytes,
        String errorMessage
) {

	/**
	 * Calcula o percentual de progresso do job.
	 *
	 * @return percentual entre 0.0 e 100.0, ou 0.0 se sem registros
	 */
	public double getProgressPercentage() {
		if (totalRecords == 0) return 0.0;
		return (double) processedRecords / totalRecords * 100.0;
	}
}