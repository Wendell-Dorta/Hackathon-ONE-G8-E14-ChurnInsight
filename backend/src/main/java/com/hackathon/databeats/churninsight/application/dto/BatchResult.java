package com.hackathon.databeats.churninsight.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO (record) que encapsula o resultado final de um job de processamento em lote.
 *
 * <p>Retornado após a conclusão do processamento para apresentar estatísticas
 * agregadas, erros ocorridos e resumo executivo do job.</p>
 *
 * @param jobId identificador único do job (correlaciona com BatchProcessingStatus)
 * @param success {@code true} se todos os registros foram processados com sucesso
 * @param totalProcessed quantidade total de registros processados
 * @param successCount quantidade de predições bem-sucedidas
 * @param errorCount quantidade de predições que falharam
 * @param processingTime timestamp de quando o processamento foi concluído
 * @param durationMs duração total do processamento em milissegundos
 * @param errors lista de mensagens de erro (vazia se sem erros)
 * @param summary descrição textual do resultado
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record BatchResult(
        String jobId,
        boolean success,
        int totalProcessed,
        int successCount,
        int errorCount,
        LocalDateTime processingTime,
        long durationMs,
        List<String> errors,
        String summary
) {}