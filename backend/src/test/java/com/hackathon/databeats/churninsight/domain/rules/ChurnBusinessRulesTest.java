package com.hackathon.databeats.churninsight.domain.rules;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para as regras de negócio de cálculo de features de churn.
 *
 * <p>Valida a engenharia de features (feature engineering):</p>
 * <ul>
 *   <li>frustration_index = skipRate * (ads_per_week + 1)</li>
 *   <li>ad_intensity = ads_per_week / ((songs_per_day * 7) + 1)</li>
 *   <li>songs_per_minute = songs_per_day / (listening_time + 1)</li>
 *   <li>is_heavy_user = listeningTime > 450 && skipRate < 0.2</li>
 *   <li>premium_no_offline = !isFree && !usaOffline</li>
 * </ul>
 *
 * <p><b>Tipo:</b> Teste unitário de regras de domínio</p>
 * <p><b>Importância:</b> CRÍTICO - Features alimentam o modelo ONNX</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
class ChurnBusinessRulesTest {

	/**
	 * Valida cálculo correto do frustration_index.
	 *
	 * <p>Fórmula: frustration_index = skipRate * (ads_listened_per_week + 1)</p>
	 * <p>Exemplo: skipRate=0.4, ads=10 → 0.4 * 11 = 4.4</p>
	 */
	@Test
	@DisplayName("Deve calcular frustration_index corretamente")
	void shouldCalculateFrustrationIndex() {
		// Arrange - skipRate=0.4, ads=10, listening_time=300, songs=20, Free
		CustomerProfile profile = createProfile(0.4, 10, 300.0, 20, "Free");

		// Act
		Map<String, Object> features = ChurnBusinessRules.calculateEngineeredFeatures(profile);

		// Assert - frustration_index = 0.4 * (10 + 1) = 4.4
		assertEquals(4.4, (Double) features.get("frustration_index"), 0.01);
	}

	/**
	 * Valida identificação de usuários "heavy users".
	 *
	 * <p>Critério: listeningTime > 450 && skipRate < 0.2</p>
	 * <p>Heavy user: 500 min, 0.1 skip = true</p>
	 * <p>Casual user: 200 min, 0.5 skip = false</p>
	 */
	@Test
	@DisplayName("Deve identificar heavy_user corretamente")
	void shouldIdentifyHeavyUser() {
		// Heavy user: listeningTime > 450 && skipRate < 0.2
		CustomerProfile heavyUser = createProfile(0.1, 0, 500.0, 30, "Premium");
		// Casual user: não atende critérios
		CustomerProfile casualUser = createProfile(0.5, 5, 200.0, 10, "Free");

		Map<String, Object> heavyFeatures = ChurnBusinessRules.calculateEngineeredFeatures(heavyUser);
		Map<String, Object> casualFeatures = ChurnBusinessRules.calculateEngineeredFeatures(casualUser);

		// Assert
		assertTrue((Boolean) heavyFeatures.get("is_heavy_user"));
		assertFalse((Boolean) casualFeatures.get("is_heavy_user"));
	}

	/**
	 * Valida cálculo de songs_per_minute (velocidade de engajamento).
	 *
	 * <p>Fórmula: songs_per_minute = songs_played_per_day / (listening_time + 1)</p>
	 * <p>Exemplo: songs=20, listening_time=100 → 20 / 101 ≈ 0.198</p>
	 */
	@Test
	@DisplayName("Deve calcular songs_per_minute corretamente")
	void shouldCalculateSongsPerMinute() {
		CustomerProfile profile = createProfile(0.2, 5, 100.0, 20, "Premium");

		Map<String, Object> features = ChurnBusinessRules.calculateEngineeredFeatures(profile);

		// Assert - songs_per_minute = 20 / (100 + 1) ≈ 0.198
		double expected = 20.0 / 101.0;
		assertEquals(expected, (Double) features.get("songs_per_minute"), 0.001);
	}

	/**
	 * Valida detecção de premium_no_offline (subutilização).
	 *
	 * <p>Flag true quando: assinatura Premium && NÃO usa offline</p>
	 * <p>Indica cliente premium que não aproveita feature offline.</p>
	 */
	@Test
	@DisplayName("Deve identificar premium_no_offline corretamente")
	void shouldIdentifyPremiumNoOffline() {
		// Premium SEM offline (subutilizando)
		CustomerProfile premiumNoOffline = createProfileWithOffline(0.1, 0, 300.0, 20, "Premium", false);
		// Premium COM offline (utilizando bem)
		CustomerProfile premiumWithOffline = createProfileWithOffline(0.1, 0, 300.0, 20, "Premium", true);
		// Free (não aplica, não tem offline)
		CustomerProfile freeUser = createProfileWithOffline(0.3, 10, 200.0, 15, "Free", false);

		Map<String, Object> features1 = ChurnBusinessRules.calculateEngineeredFeatures(premiumNoOffline);
		Map<String, Object> features2 = ChurnBusinessRules.calculateEngineeredFeatures(premiumWithOffline);
		Map<String, Object> features3 = ChurnBusinessRules.calculateEngineeredFeatures(freeUser);

		// Assert
		assertTrue((Boolean) features1.get("premium_no_offline"));   // Premium sem offline
		assertFalse((Boolean) features2.get("premium_no_offline"));  // Premium com offline
		assertFalse((Boolean) features3.get("premium_no_offline"));  // Free user
	}

	/**
	 * Valida comportamento com valores nulos (tratamento defensivo).
	 *
	 * <p>Teste: Método deve tratar null values como zero
	 * sem lançar NullPointerException.</p>
	 */
	@Test
	@DisplayName("Deve tratar valores nulos corretamente")
	void shouldHandleNullValues() {
		// Arrange - criar perfil com alguns campos null
		CustomerProfile profile = CustomerProfile.builder()
				.userId("test")
				.gender("Male")
				.age(25)
				.country("BR")
				.subscriptionType("Free")
				.deviceType("Mobile")
				// Deixa campos numéricos como null
				.build();

		// Act & Assert - não deve lançar exceção
		assertDoesNotThrow(() -> ChurnBusinessRules.calculateEngineeredFeatures(profile));

		Map<String, Object> features = ChurnBusinessRules.calculateEngineeredFeatures(profile);
		assertNotNull(features);
		assertEquals(5, features.size()); // Deve ter as 5 features
	}

	/**
	 * Helper para criar perfil com offline_listening implicitamente false.
	 */
	private CustomerProfile createProfile(double skipRate, int ads, double listeningTime,
										  int songsPerDay, String subscriptionType) {
		return createProfileWithOffline(skipRate, ads, listeningTime, songsPerDay, subscriptionType, false);
	}

	/**
	 * Helper para criar perfil com todos os parâmetros incluindo offline_listening.
	 */
	private CustomerProfile createProfileWithOffline(double skipRate, int ads, double listeningTime,
													 int songsPerDay, String subscriptionType, boolean offline) {
		return CustomerProfile.builder()
				.userId("test-user")
				.gender("Male")
				.age(28)
				.country("BR")
				.subscriptionType(subscriptionType)
				.deviceType("Mobile")
				.listeningTime(listeningTime)
				.songsPlayedPerDay(songsPerDay)
				.skipRate(skipRate)
				.adsListenedPerWeek(ads)
				.offlineListening(offline)
				.build();
	}
}

