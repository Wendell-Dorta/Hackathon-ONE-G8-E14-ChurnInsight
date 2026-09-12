package com.hackathon.databeats.churninsight.infra.exception;

import java.time.Instant;

/**
 * DTO (record) para resposta padronizada de erros da API.
 *
 * <p>Utilizado pelo {@link GlobalExceptionHandler} para normalizar respostas
 * de erro com informações técnicas úteis para cliente e debugging.</p>
 *
 * <p><b>Exemplo de resposta 400:</b></p>
 * <pre>{@code
 * {
 *   "timestamp": "2026-01-09T10:30:45.123Z",
 *   "status": 400,
 *   "error": "user_id: ID do usuário é obrigatório, age: Idade mínima permitida: 10 anos",
 *   "path": "/predict"
 * }
 * }</pre>
 *
 * @param timestamp instant do erro (UTC)
 * @param status código HTTP de status (400, 422, 500, etc)
 * @param error mensagem de erro descritiva (pode conter múltiplas validações)
 * @param path caminho HTTP da requisição que falhou
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record ApiErrorResponse(
		Instant timestamp,
		int status,
		String error,
		String path
) {}