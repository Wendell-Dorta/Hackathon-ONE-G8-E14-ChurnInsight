package com.hackathon.databeats.churninsight.application.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO genérico de resposta paginada para listagens de predições.
 *
 * <p>Encapsula dados paginados com metadados de navegação e estatísticas opcionais
 * da página atual. Reutilizável para qualquer tipo de conteúdo.</p>
 *
 * @param <T> tipo do conteúdo (ex: PredictionHistory, ClientPrediction)
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Builder
public record PaginatedResponse<T>(

    /** Registros da página atual. */
    List<T> content,

    /** Número da página (0-indexed). */
    int page,

    /** Tamanho máximo de registros por página. */
    int size,

    /** Quantidade total de registros disponíveis. */
    long totalElements,

    /** Quantidade total de páginas. */
    int totalPages,

    /** {@code true} se é a primeira página. */
    boolean first,

    /** {@code true} se é a última página. */
    boolean last,

    /** {@code true} se existe próxima página. */
    boolean hasNext,

    /** {@code true} se existe página anterior. */
    boolean hasPrevious,

    /** Estatísticas da página atual (opcional, pode ser null). */
    PageStats stats
) {

    /**
     * Estatísticas agregadas dos registros presentes na página.
     *
     * <p>Proporciona insights rápidos sem necessidade de processamento adicional
     * do cliente.</p>
     *
     * @author Equipe ChurnInsight
     * @version 1.0.0
     */
    @Builder
    public static record PageStats(
        /** Quantidade de clientes que vão cancelar nesta página. */
        long willChurnCount,

        /** Quantidade de clientes que vão permanecer nesta página. */
        long willStayCount,

        /** Média de probabilidade de churn dos registros da página. */
        double avgProbability
    ) {}
}
