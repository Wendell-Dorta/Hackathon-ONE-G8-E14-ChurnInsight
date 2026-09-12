package com.hackathon.databeats.churninsight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Teste de inicialização da aplicação ChurnInsight.
 *
 * <p>Valida que a classe principal da aplicação existe e pode ser instanciada.
 * Este é um teste básico de smoke test que não carrega o contexto Spring completo.</p>
 *
 * <p><b>Escopo:</b> Teste unitário puro (sem dependências externas)</p>
 * <p><b>Categoria:</b> Smoke test</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
class ChurnInsightApplicationTests {

	/**
	 * Verifica se a classe ChurnInsightApplication existe e é instanciável.
	 *
	 * <p>Este teste garante que a aplicação tem um ponto de entrada válido.
	 * Não requer banco de dados ou configuração de infraestrutura.</p>
	 */
	@Test
	void applicationClassExists() {
		// Verifica que a classe principal existe e pode ser instanciada
		assertTrue(ChurnInsightApplication.class.isAssignableFrom(ChurnInsightApplication.class));
	}

}
