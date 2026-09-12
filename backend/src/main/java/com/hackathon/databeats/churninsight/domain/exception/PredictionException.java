package com.hackathon.databeats.churninsight.domain.exception;

/**
 * Exceção de domínio para erros de negócio durante a predição de churn.
 *
 * <p>Lançada quando ocorre falha em regras de negócio ou processamento
 * que não são erros técnicos de infraestrutura. Exemplos:</p>
 * <ul>
 *   <li>Dados de entrada inválidos após validação de domínio</li>
 *   <li>Perfil de cliente com valores inconsistentes</li>
 *   <li>Falha na execução de regras de negócio</li>
 * </ul>
 *
 * <h3>Diferença de ModelInferenceException:</h3>
 * <ul>
 *   <li><b>PredictionException:</b> Erro de negócio/domínio (422 Unprocessable Entity)</li>
 *   <li><b>ModelInferenceException:</b> Erro técnico ONNX (500 Internal Server Error)</li>
 * </ul>
 *
 * <h3>Tratamento:</h3>
 * <p>Convertida para HTTP 422 pelo {@code GlobalExceptionHandler}.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 * @see com.hackathon.databeats.churninsight.infra.exception.GlobalExceptionHandler
 */
public class PredictionException extends RuntimeException {

    /**
     * Cria nova exceção de predição com mensagem descritiva.
     *
     * @param message descrição do erro de negócio
     */
    public PredictionException(String message) {
        super(message);
    }

    /**
     * Cria nova exceção de predição com mensagem e causa raiz.
     *
     * @param message descrição do erro de negócio
     * @param cause exceção que causou o erro
     */
    public PredictionException(String message, Throwable cause) {
        super(message, cause);
    }
}