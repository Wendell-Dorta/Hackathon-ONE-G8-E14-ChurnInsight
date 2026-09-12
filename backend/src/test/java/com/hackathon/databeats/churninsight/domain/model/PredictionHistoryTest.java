package com.hackathon.databeats.churninsight.domain.model;

import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o modelo de domínio PredictionHistory.
 *
 * <p>Valida:</p>
 * <ul>
 *   <li>Factory method fromPrediction() que mapeia dados de predição</li>
 *   <li>Builder para construção direta</li>
 *   <li>Preenchimento de metadados de auditoria</li>
 *   <li>Mapeamento de features calculadas</li>
 * </ul>
 *
 * <p><b>Tipo:</b> Teste unitário de modelo de domínio</p>
 * <p><b>Dependências:</b> CustomerProfile (mock), ChurnStatus (enum)</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
class PredictionHistoryTest {

	/**
	 * Valida factory method fromPrediction() que cria histórico a partir de predição.
	 *
	 * <p>Teste: Factory method deve mapear corretamente todos os dados:
	 * - Dados do cliente (userId, gender, etc)
	 * - Resultado da predição (status, probability)
	 * - Features calculadas (frustration_index, is_heavy_user, etc)
	 * - Metadados de auditoria (requesterId, requestIp, createdAt)</p>
	 */
	@Test
	@DisplayName("Deve criar histórico a partir de predição usando factory method")
	void shouldCreateHistoryFromPrediction() {
		// Arrange - preparar dados de teste
		CustomerProfile profile = createTestProfile();
		Map<String, Object> features = Map.of(
				"frustration_index", 2.5,
				"ad_intensity", 0.3,
				"songs_per_minute", 0.15,
				"is_heavy_user", true,
				"premium_no_offline", false
		);

		// Act - executar factory method
		PredictionHistory history = PredictionHistory.fromPrediction(
				"test-id-123",
				profile,
				ChurnStatus.WILL_CHURN,
				0.78,
				features,
				"requester-001",
				"192.168.1.1"
		);

		// Assert - validar mapeamento completo
		assertEquals("test-id-123", history.id());
		assertEquals("user-001", history.userId());
		assertEquals(ChurnStatus.WILL_CHURN, history.churnStatus());
		assertEquals(0.78, history.probability());
		assertEquals(2.5, history.frustrationIndex());
		assertTrue(history.isHeavyUser());
		assertEquals("requester-001", history.requesterId());
		assertEquals("192.168.1.1", history.requestIp());
		assertNotNull(history.createdAt()); // Timestamp deve ser preenchido
	}

	/**
	 * Valida builder direto para construção de histórico.
	 *
	 * <p>Teste: Builder deve permitir construção com campos selecionados
	 * sem necessidade de factory method.</p>
	 */
	@Test
	@DisplayName("Deve criar histórico usando builder diretamente")
	void shouldCreateHistoryUsingBuilder() {
		// Act - construir via builder
		PredictionHistory history = PredictionHistory.builder()
				.id("id-456")
				.userId("user-456")
				.churnStatus(ChurnStatus.WILL_STAY)
				.probability(0.25)
				.build();

		// Assert - validar campos preenchidos
		assertEquals("id-456", history.id());
		assertEquals("user-456", history.userId());
		assertEquals(ChurnStatus.WILL_STAY, history.churnStatus());
		assertEquals(0.25, history.probability());
	}

	/**
	 * Helper para criar perfil de cliente para testes.
	 *
	 * <p>Cria um perfil Premium com valores realistas para validação
	 * do factory method.</p>
	 *
	 * @return CustomerProfile pré-configurado para testes
	 */
	private CustomerProfile createTestProfile() {
		return CustomerProfile.builder()
				.userId("user-001")
				.gender("Female")
				.age(25)
				.country("US")
				.subscriptionType("Premium")
				.deviceType("Desktop")
				.listeningTime(500.0)
				.songsPlayedPerDay(30)
				.skipRate(0.1)
				.adsListenedPerWeek(0)
				.offlineListening(true)
				.build();
	}
}

