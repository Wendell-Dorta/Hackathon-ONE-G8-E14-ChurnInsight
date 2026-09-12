package com.hackathon.databeats.churninsight.infra.exception;

/**
 * Exceção de infraestrutura para erros na inferência do modelo ONNX.
 *
 * <p>Lançada quando ocorre falha técnica na execução do modelo de ML:
 * carregamento do modelo, formato de input inválido, erro de memória, etc.</p>
 *
 * <p><b>Diferente de:</b></p>
 * <ul>
 *   <li>{@link com.hackathon.databeats.churninsight.domain.exception.PredictionException} -
 *       erro de negócio (dados inválidos)</li>
 * </ul>
 *
 * <p><b>Tratamento:</b> Convertida para HTTP 500 pelo {@link GlobalExceptionHandler}.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public class ModelInferenceException extends RuntimeException {

	/**
	 * Cria nova exceção com mensagem de erro.
	 *
	 * @param message descrição técnica do erro
	 */
	public ModelInferenceException(String message) {
		super(message);
	}

	/**
	 * Cria nova exceção com mensagem e causa raiz.
	 *
	 * @param message descrição técnica do erro
	 * @param cause exceção que causou o erro (ex: OrtException)
	 */
	public ModelInferenceException(String message, Throwable cause) {
		super(message, cause);
	}
}