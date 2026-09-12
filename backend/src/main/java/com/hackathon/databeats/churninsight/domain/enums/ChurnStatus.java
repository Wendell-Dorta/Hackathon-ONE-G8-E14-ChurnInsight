package com.hackathon.databeats.churninsight.domain.enums;

/**
 * Enum que representa o resultado da classificação de churn de um cliente.
 *
 * <p>Define as duas classes possíveis de saída do modelo de Machine Learning:
 * cliente que vai cancelar (churn) ou cliente que vai permanecer (stay).</p>
 *
 * <h3>Mapeamento do Modelo ONNX:</h3>
 * <ul>
 *   <li><b>Classe 0:</b> WILL_STAY (permanência)</li>
 *   <li><b>Classe 1:</b> WILL_CHURN (cancelamento)</li>
 * </ul>
 *
 * <h3>Threshold de Decisão:</h3>
 * <p>A classificação é determinada comparando a probabilidade de churn
 * com o {@code threshold_otimo} definido nos metadados do modelo.
 * Se {@code P(churn) >= threshold}, o status é WILL_CHURN.</p>
 *
 * <h3>Uso Típico:</h3>
 * <pre>{@code
 * double probability = 0.78;
 * double threshold = 0.431;
 * ChurnStatus status = probability >= threshold
 *     ? ChurnStatus.WILL_CHURN
 *     : ChurnStatus.WILL_STAY;
 * }</pre>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public enum ChurnStatus {

    /**
     * Cliente classificado com alta probabilidade de cancelamento.
     *
     * <p>Requer ação proativa do time de retenção. O diagnóstico de IA
     * fornecerá detalhes sobre fatores de risco e ações sugeridas.</p>
     */
    WILL_CHURN,

    /**
     * Cliente classificado com baixa probabilidade de cancelamento.
     *
     * <p>Cliente em situação estável. Monitoramento preventivo recomendado
     * para identificar mudanças de comportamento.</p>
     */
    WILL_STAY
}