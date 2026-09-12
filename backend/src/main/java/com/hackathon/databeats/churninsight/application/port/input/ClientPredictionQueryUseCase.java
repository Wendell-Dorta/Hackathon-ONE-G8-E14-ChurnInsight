package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;

import java.util.List;
import java.util.Optional;

/**
 * Port de entrada para consulta de predições.
 *
 * <p>Responsável por expor as operações de busca de dados processados.
 * Agora conectado à camada de persistência para suportar grandes volumes de dados.</p>
 *
 * @author Equipe ChurnInsight
 */
public interface ClientPredictionQueryUseCase {

	/**
	 * Busca a predição de um cliente pelo ID.
	 */
	Optional<ClientPrediction> findByClientId(String clientId);

	/**
	 * Lista predições de forma paginada.
	 * <p>Alterado para exigir paginação e evitar OOM (Out Of Memory).</p>
	 *
	 * @param page número da página (0-indexed)
	 * @param size tamanho da página
	 * @return lista parcial de predições
	 */
	List<ClientPrediction> findAll(int page, int size);

	/**
	 * Lista clientes com probabilidade de churn acima do threshold.
	 *
	 * @param thresholdProbability limite mínimo (0.0 a 1.0)
	 * @param page número da página
	 * @param size tamanho da página
	 */
	List<ClientPrediction> findHighRiskClients(double thresholdProbability, int page, int size);

	/**
	 * Lista clientes cuja predição final é "Vai Cancelar" (churn = true).
	 */
	List<ClientPrediction> findClientsWhoWillChurn(int page, int size);

	/**
	 * Obtém a contagem total real do banco de dados.
	 */
	long getTotalClients();

	/**
	 * Obtém estatísticas calculadas via agregação no banco de dados.
	 */
	ClientStatistics getStatistics();

	// DTO (Record) mantém-se igual
	record ClientStatistics(
			long totalClients,
			long clientsWillChurn,
			long clientsWillStay,
			double averageChurnProbability,
			double maxChurnProbability,
			double minChurnProbability
	) {}
}