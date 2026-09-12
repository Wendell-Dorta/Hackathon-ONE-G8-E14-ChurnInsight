package com.hackathon.databeats.churninsight.application.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO para filtros de busca avançada de histórico de predições.
 *
 * <p>Todos os campos são opcionais. Quando fornecidos, são combinados com lógica AND.</p>
 *
 * <p><b>Categorias de filtro:</b></p>
 * <ul>
 *   <li><b>Status:</b> churnStatus (Vai Cancelar/Vai Continuar)</li>
 *   <li><b>Probabilidade:</b> minProbability, maxProbability</li>
 *   <li><b>Demográfico:</b> gender, age (range), country, subscription_type, device_type</li>
 *   <li><b>Temporal:</b> startDate, endDate (data da predição)</li>
 *   <li><b>Comportamento:</b> isHeavyUser, offlineListening</li>
 *   <li><b>Features:</b> frustrationIndex (range)</li>
 *   <li><b>Texto:</b> userId (busca parcial)</li>
 * </ul>
 *
 * <p><b>Exemplo de uso:</b></p>
 * <pre>{@code
 * PredictionSearchFilter filter = PredictionSearchFilter.builder()
 *   .churnStatus(ChurnStatus.WILL_CHURN)
 *   .minProbability(0.7)
 *   .subscriptionType("Premium")
 *   .country("BR")
 *   .build();
 * }</pre>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Builder
public record PredictionSearchFilter(

    /** Filtra por status de classificação de churn. */
    ChurnStatus churnStatus,

    /** Probabilidade mínima de churn (0.0 a 1.0). */
    Double minProbability,

    /** Probabilidade máxima de churn (0.0 a 1.0). */
    Double maxProbability,

    /** Gênero do cliente. */
    String gender,

    /** Idade mínima do cliente em anos. */
    Integer minAge,

    /** Idade máxima do cliente em anos. */
    Integer maxAge,

    /** País de origem (ISO code). */
    String country,

    /** Tipo de assinatura. */
    String subscriptionType,

    /** Tipo de dispositivo primário. */
    String deviceType,

    /** Data inicial da predição (inclusive). */
    LocalDate startDate,

    /** Data final da predição (inclusive). */
    LocalDate endDate,

    /** Filtra por usuários classificados como intensos. */
    Boolean isHeavyUser,

    /** Filtra por uso do recurso de escuta offline. */
    Boolean offlineListening,

    /** Índice de frustração mínimo (0.0+). */
    Double minFrustrationIndex,

    /** Índice de frustração máximo. */
    Double maxFrustrationIndex,

    /** Busca por ID do cliente (busca contém). */
    String userId
) {}

