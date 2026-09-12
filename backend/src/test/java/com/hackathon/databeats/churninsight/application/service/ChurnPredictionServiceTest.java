package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o serviço de predição de churn (ChurnPredictionService).
 *
 * <p>Valida:</p>
 * <ul>
 *   <li>Classificação baseada em threshold</li>
 *   <li>Persistência de histórico</li>
 *   <li>Cálculo de probabilidades</li>
 *   <li>Mapeamento de features</li>
 * </ul>
 *
 * <p><b>Arquitetura:</b> Usa mocks para InferencePort e SaveHistoryPort
 * seguindo padrão Hexagonal (Ports & Adapters).</p>
 *
 * <p><b>Tipo:</b> Teste unitário de serviço de aplicação</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ChurnPredictionServiceTest {

	// Mocks dos ports (contratos de infraestrutura)
	@Mock
	private SaveHistoryPort saveHistoryPort;

	@Mock
	private InferencePort inferencePort;

	@Mock
	private ModelMetadata metadata;

	// Serviço sob teste
	private ChurnPredictionService churnPredictionService;

	/**
	 * Setup executado antes de cada teste.
	 * Inicializa o serviço com mocks.
	 */
	@BeforeEach
	void setUp() {
		churnPredictionService = new ChurnPredictionService(saveHistoryPort, inferencePort, metadata);
	}

	/**
	 * Valida classificação como WILL_CHURN quando probabilidade >= threshold.
	 *
	 * <p>Setup: threshold=0.5, probabilidade=0.8
	 * Resultado esperado: WILL_CHURN</p>
	 */
	@Test
	@DisplayName("Deve retornar WILL_CHURN quando probabilidade >= threshold")
	void shouldReturnWillChurnWhenProbabilityAboveThreshold() {
		// Arrange - mock retorna [P(stay)=0.2, P(churn)=0.8]
		CustomerProfile profile = createTestProfile();
		float[] mockPrediction = {0.2f, 0.8f}; // [STAY, CHURN]

		when(inferencePort.predict(any(CustomerProfile.class), any(Map.class)))
				.thenReturn(mockPrediction);
		when(metadata.getThresholdOtimo()).thenReturn(0.5);

		// Act
		PredictionResult result = churnPredictionService.predictWithStats(profile, "test-user", "127.0.0.1");

		// Assert
		assertNotNull(result);
		assertEquals(ChurnStatus.WILL_CHURN, result.label());
		assertEquals(0.8, result.probability(), 0.001);
	}

	/**
	 * Valida classificação como WILL_STAY quando probabilidade < threshold.
	 *
	 * <p>Setup: threshold=0.5, probabilidade=0.3
	 * Resultado esperado: WILL_STAY</p>
	 */
	@Test
	@DisplayName("Deve retornar WILL_STAY quando probabilidade < threshold")
	void shouldReturnWillStayWhenProbabilityBelowThreshold() {
		// Arrange - mock retorna [P(stay)=0.7, P(churn)=0.3]
		CustomerProfile profile = createTestProfile();
		float[] mockPrediction = {0.7f, 0.3f}; // [STAY, CHURN]

		when(inferencePort.predict(any(CustomerProfile.class), any(Map.class)))
				.thenReturn(mockPrediction);
		when(metadata.getThresholdOtimo()).thenReturn(0.5);

		// Act
		PredictionResult result = churnPredictionService.predictWithStats(profile, "test-user", "127.0.0.1");

		// Assert
		assertNotNull(result);
		assertEquals(ChurnStatus.WILL_STAY, result.label());
		assertEquals(0.3, result.probability(), 0.001);
	}

	/**
	 * Valida que o histórico é persistido ao chamar predict().
	 *
	 * <p>Verifica que SaveHistoryPort.save() é invocado exatamente uma vez.</p>
	 */
	@Test
	@DisplayName("Deve salvar histórico de predição corretamente")
	void shouldSaveHistoryCorrectly() {
		// Arrange
		CustomerProfile profile = createTestProfile();
		float[] mockPrediction = {0.4f, 0.6f};

		when(inferencePort.predict(any(CustomerProfile.class), any(Map.class)))
				.thenReturn(mockPrediction);
		when(metadata.getThresholdOtimo()).thenReturn(0.5);

		// Act
		churnPredictionService.predict(profile, "test-user", "192.168.1.1");

		// Assert - verificar que save foi chamado
		verify(saveHistoryPort, times(1)).save(any());
	}

	/**
	 * Valida retorno de probabilidades das duas classes.
	 *
	 * <p>Verifica que classProbabilities contém:
	 * - WILL_CHURN: probabilidade de churn
	 * - WILL_STAY: probabilidade de permanência</p>
	 */
	@Test
	@DisplayName("Deve conter probabilidades das duas classes no resultado")
	void shouldContainBothClassProbabilities() {
		// Arrange
		CustomerProfile profile = createTestProfile();
		float[] mockPrediction = {0.35f, 0.65f}; // [STAY, CHURN]

		when(inferencePort.predict(any(CustomerProfile.class), any(Map.class)))
				.thenReturn(mockPrediction);
		when(metadata.getThresholdOtimo()).thenReturn(0.5);

		// Act
		PredictionResult result = churnPredictionService.predictWithStats(profile, "test-user", "127.0.0.1");

		// Assert
		assertNotNull(result.classProbabilities());
		assertTrue(result.classProbabilities().containsKey("WILL_CHURN"));
		assertTrue(result.classProbabilities().containsKey("WILL_STAY"));
		assertEquals(0.65f, result.classProbabilities().get("WILL_CHURN"), 0.001);
		assertEquals(0.35f, result.classProbabilities().get("WILL_STAY"), 0.001);
	}

	/**
	 * Helper para criar perfil de cliente realista para testes.
	 *
	 * @return CustomerProfile pré-configurado
	 */
	private CustomerProfile createTestProfile() {
		return CustomerProfile.builder()
				.userId("test-user-001")
				.gender("Male")
				.age(30)
				.country("BR")
				.subscriptionType("Premium")
				.listeningTime(450.0)
				.songsPlayedPerDay(25)
				.skipRate(0.15)
				.adsListenedPerWeek(0)
				.deviceType("Mobile")
				.offlineListening(true)
				.build();
	}
}

