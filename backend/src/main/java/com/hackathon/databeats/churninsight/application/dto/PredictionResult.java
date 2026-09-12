package com.hackathon.databeats.churninsight.application.dto;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import lombok.Builder;

import java.util.Map;

/**
 * DTO de aplicação para resultado de predição.
 *
 * <p>Encapsula o resultado da inferência do modelo, incluindo o status
 * classificado, probabilidade e distribuição de probabilidades por classe.</p>
 *
 * <p>Este DTO pertence à camada de aplicação, garantindo que os ports de entrada
 * não dependam de classes de infraestrutura.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Builder
public record PredictionResult(
        /** Status de classificação (WILL_CHURN ou WILL_STAY). */
        ChurnStatus label,

        /** Probabilidade de churn (0.0 a 1.0). */
        double probability,

        /** Array com probabilidades brutas [P(stay), P(churn)]. */
        float[] probabilities,

        /** Mapa de probabilidades por nome da classe. */
        Map<String, Float> classProbabilities
) {}
