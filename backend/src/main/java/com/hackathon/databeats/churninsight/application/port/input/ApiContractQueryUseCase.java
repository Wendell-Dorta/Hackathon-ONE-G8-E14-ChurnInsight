package com.hackathon.databeats.churninsight.application.port.input;

import com.hackathon.databeats.churninsight.application.dto.ApiContract;

/**
 * Port de entrada para consulta do contrato (especificação) da API.
 *
 * <p>Fornece informações sobre o schema esperado, exemplos de uso e metadados
 * do modelo de ML. Útil para documentação, testes e validação de clientes.</p>
 *
 * <p><b>Implementações:</b></p>
 * <ul>
 *   <li>{@link com.hackathon.databeats.churninsight.application.service.ApiContractQueryService}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface ApiContractQueryUseCase {

	/**
	 * Obtém o contrato completo da API de predição.
	 *
	 * <p>Inclui especificação de inputs esperados, outputs garantidos,
	 * códigos de erro e exemplos de uso.</p>
	 *
	 * @return {@link ApiContract} com especificação completa
	 */
	ApiContract getContract();

	/**
	 * Retorna um payload de exemplo válido para testes.
	 *
	 * @return exemplo de entrada com valores realistas
	 */
	ApiContract.PayloadInput getSamplePayload();

	/**
	 * Retorna a resposta esperada para o payload de exemplo.
	 *
	 * @return exemplo de saída correspondente ao payload
	 */
	ApiContract.ExpectedResponse getExpectedResponse();

	/**
	 * Obtém informações técnicas do modelo de ML.
	 *
	 * @return metadados incluindo versão, acurácia, recall, F1-score
	 */
	ApiContract.Metadata getModelInfo();
}

