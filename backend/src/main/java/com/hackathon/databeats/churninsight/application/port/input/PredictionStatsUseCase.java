package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

/**
 * Port de entrada que define o caso de uso para predição com retorno de estatísticas.
 *
 * <p>Diferentemente de {@link PredictChurnUseCase}, não persiste o resultado.
 * Retorna as estatísticas completas (probabilidades brutas e por classe) para
 * processamento do cliente.</p>
 *
 * <p><b>Uso Típico:</b> Endpoints que precisam retornar análise detalhada sem
 * registrar no histórico de auditoria.</p>
 *
 * <p><b>Implementações:</b></p>
 * <ul>
 *   <li>{@link com.hackathon.databeats.churninsight.application.service.ChurnPredictionService}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface PredictionStatsUseCase {

	/**
	 * Executa predição de churn e retorna estatísticas detalhadas.
	 *
	 * <p>O resultado não é persistido automaticamente, apenas computado.
	 * Use {@link PredictChurnUseCase#predict} se precisar de auditoria.</p>
	 *
	 * @param profile dados do cliente para predição
	 * @param requesterId identificador do usuário solicitante
	 * @param requestIp endereço IP da requisição
	 * @return {@link PredictionResult} contendo label, probability e distribuição por classe
	 * @throws com.hackathon.databeats.churninsight.domain.exception.PredictionException
	 *         se ocorrer erro na inferência
	 */
	PredictionResult predictWithStats(CustomerProfile profile, String requesterId, String requestIp);
}