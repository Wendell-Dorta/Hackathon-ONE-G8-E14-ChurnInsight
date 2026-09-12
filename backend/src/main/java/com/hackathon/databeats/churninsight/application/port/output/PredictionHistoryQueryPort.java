package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.application.dto.PaginatedResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionHistoryResponse;
import com.hackathon.databeats.churninsight.application.dto.PredictionSearchFilter;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;

import java.util.List;

/**
 * Port de saída para consulta do histórico de predições.
 *
 * <p>Isola a camada de aplicação dos detalhes de acesso a dados (JPA, Specifications).
 * Todo código que dependa de PredictionHistoryRepository deve fazer isso via este contrato.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface PredictionHistoryQueryPort {

    /** Conta o total de predições persistidas. */
    long count();

    /** Conta predições por status de churn. */
    long countByChurnStatus(ChurnStatus status);

    /** Conta quantos clientes estão no TOP 25% por probabilidade. */
    Long countTop25AtRisk();

    /** Retorna tupla com contagens agregadas de fatores de risco. */
    Object[] getRiskFactorCounts();

    /** Retorna tupla com contagens por faixas de probabilidade (4 buckets). */
    Object[] getProbabilityBuckets();

    /** Retorna tupla com estatísticas globais (total, avg, churners, stayers). */
    Object[] getGlobalStats();

    /** Retorna pares [subscription_type, count] do TOP 25% por probabilidade. */
    List<Object[]> getTop25SubscriptionCounts();

    /** Contagem de predições agrupada por gênero. */
    List<Object[]> countByGender();

    /** Contagem de predições agrupada por tipo de assinatura. */
    List<Object[]> countBySubscriptionType();

    /** Busca userId por prefixo (para autocomplete). */
    List<String> findUserIdsByPrefix(String prefix, int limit);

    /**
     * Busca paginada com filtros dinâmicos.
     *
     * @param filter critérios de filtragem (todos opcionais)
     * @param page número da página (0-indexed, já validado)
     * @param size tamanho da página (já limitado ao máximo)
     * @param sortBy campo de ordenação
     * @param sortDir direção da ordenação (asc/desc)
     */
    PaginatedResponse<PredictionHistoryResponse> search(
            PredictionSearchFilter filter, int page, int size, String sortBy, String sortDir);
}
