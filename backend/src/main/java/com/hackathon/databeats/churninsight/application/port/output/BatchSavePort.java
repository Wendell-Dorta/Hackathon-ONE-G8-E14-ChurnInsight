package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;

import java.util.List;

/**
 * Port de saída para persistência otimizada de predições em lote.
 *
 * <p>Define o contrato para operações de inserção em massa otimizadas para
 * performance. Implementações devem utilizar JDBC batch insert e transações
 * controladas para melhor throughput em processamento de grandes volumes.</p>
 *
 * <p><b>Otimizações esperadas:</b></p>
 * <ul>
 *   <li>JDBC batch insert (não ORM)</li>
 *   <li>Transações controladas por tamanho de batch</li>
 *   <li>Desabilitação de auto-flush</li>
 *   <li>Suporte para rollback seletivo (não falhar tudo por 1 erro)</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface BatchSavePort {

	/**
	 * Salva múltiplas predições de uma vez.
	 *
	 * <p>Implementação deve usar JDBC batch insert com uma única transação.
	 * Ideal para volumes pequenos/médios.</p>
	 *
	 * @param histories lista de predições para persistir
	 * @throws RuntimeException se erro de banco de dados
	 */
	void saveAll(List<PredictionHistory> histories);

	/**
	 * Salva predições divididas em batches com transações independentes.
	 *
	 * <p>Cada batch é uma transação separada. Reduz memória usada e permite
	 * rollback granular. Ideal para volumes muito grandes (>100k registros).</p>
	 *
	 * @param histories lista completa de predições para persistir
	 * @param batchSize tamanho de cada batch (ex: 5000)
	 * @throws RuntimeException se erro de banco de dados
	 */
	void saveBatch(List<PredictionHistory> histories, int batchSize);

	/**
	 * Conta o total de predições persistidas no banco.
	 *
	 * @return quantidade total de registros de predição
	 */
	long countTotalPredictions();
}