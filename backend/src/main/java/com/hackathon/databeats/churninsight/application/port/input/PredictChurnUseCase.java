package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

/**
 * Port de entrada que define o caso de uso para predição de churn com persistência.
 *
 * <p>Contrato da camada de aplicação para predições que devem ser auditadas e
 * persistidas no banco de dados. A predição é executada e o resultado é armazenado
 * automaticamente no histórico de predições.</p>
 *
 * <p><b>Implementações:</b></p>
 * <ul>
 *   <li>{@link com.hackathon.databeats.churninsight.application.service.ChurnPredictionService}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface PredictChurnUseCase {

	/**
	 * Executa predição de churn e persiste o resultado.
	 *
	 * <p>O fluxo é:</p>
	 * <ol>
	 *   <li>Calcular features derivadas a partir do perfil do cliente</li>
	 *   <li>Executar inferência do modelo ONNX</li>
	 *   <li>Classificar resultado baseado no threshold ótimo do modelo</li>
	 *   <li>Persister histórico com metadados de auditoria</li>
	 * </ol>
	 *
	 * <p><b>Auditoria:</b> Todos os resultados são registrados com:</p>
	 * <ul>
	 *   <li>ID único (UUIDv7)</li>
	 *   <li>IP do requisitante</li>
	 *   <li>Timestamp de execução</li>
	 *   <li>Features calculadas</li>
	 * </ul>
	 *
	 * @param profile dados do cliente para predição
	 * @param requesterId identificador do usuário solicitante
	 * @param requestIp endereço IP da requisição
	 * @throws com.hackathon.databeats.churninsight.domain.exception.PredictionException
	 *         se ocorrer erro na inferência ou persistência
	 * @return resultado da predição, com probabilidades e label, para evitar segunda
	 *         chamada ao modelo quando o chamador também precisa dos dados
	 */
	PredictionResult predict(CustomerProfile profile, String requesterId, String requestIp);
}