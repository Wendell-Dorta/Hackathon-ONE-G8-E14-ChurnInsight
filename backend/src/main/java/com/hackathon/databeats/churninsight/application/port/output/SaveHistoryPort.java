package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;

/**
 * Port de saída para persistência do histórico de predições.
 *
 * <p>Define o contrato para armazenamento de predições de churn com foco em auditoria.
 * Utiliza modelo de domínio ({@link com.hackathon.databeats.churninsight.domain.model.PredictionHistory})
 * em vez de entidades JPA, garantindo isolamento da camada de aplicação de detalhes
 * de infraestrutura.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface SaveHistoryPort {

	/**
	 * Persiste um histórico de predição no banco de dados.
	 *
	 * @param history modelo de domínio contendo resultado da predição e metadados
	 * @throws com.hackathon.databeats.churninsight.infra.exception.ModelInferenceException
	 *         se ocorrer erro de persistência
	 */
	void save(PredictionHistory history);
}