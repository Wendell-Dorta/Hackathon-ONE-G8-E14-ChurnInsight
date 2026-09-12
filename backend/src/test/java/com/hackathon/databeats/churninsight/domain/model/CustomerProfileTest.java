package com.hackathon.databeats.churninsight.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o modelo de domínio CustomerProfile.
 *
 * <p>Valida:</p>
 * <ul>
 *   <li>Identificação de tipos de assinatura (premium, free, etc)</li>
 *   <li>Construção com builder</li>
 *   <li>Imutabilidade do modelo</li>
 *   <li>Validações case-insensitive</li>
 * </ul>
 *
 * <p><b>Tipo:</b> Teste unitário de modelo de domínio</p>
 * <p><b>Dependências:</b> Nenhuma (modelo puro)</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
class CustomerProfileTest {

	/**
	 * Valida identificação de usuários Premium com case insensitivity.
	 *
	 * <p>Teste: isPremium() deve retornar true apenas para subscription "Premium"
	 * (independente de maiúsculas/minúsculas).</p>
	 */
	@Test
	@DisplayName("Deve identificar usuário Premium corretamente")
	void shouldIdentifyPremiumUser() {
		CustomerProfile premium = createProfile("Premium");
		CustomerProfile free = createProfile("Free");
		CustomerProfile premiumLower = createProfile("premium");

		assertTrue(premium.isPremium());
		assertFalse(free.isPremium());
		assertTrue(premiumLower.isPremium());
	}

	/**
	 * Valida construção de CustomerProfile com todos os campos via builder.
	 *
	 * <p>Teste: Builder deve criar instância com todos os 11 campos
	 * e getters devem retornar valores corretos.</p>
	 */
	@Test
	@DisplayName("Deve criar perfil com todos os campos preenchidos")
	void shouldCreateProfileWithAllFields() {
		CustomerProfile profile = CustomerProfile.builder()
				.userId("user-123")
				.gender("Male")
				.age(30)
				.country("BR")
				.subscriptionType("Premium")
				.listeningTime(450.0)
				.songsPlayedPerDay(25)
				.skipRate(0.15)
				.adsListenedPerWeek(5)
				.deviceType("Mobile")
				.offlineListening(true)
				.build();

		// Valida cada campo
		assertEquals("Male", profile.gender());
		assertEquals(30, profile.age());
		assertEquals("BR", profile.country());
		assertEquals("Premium", profile.subscriptionType());
		assertEquals(450.0, profile.listeningTime());
		assertEquals(25, profile.songsPlayedPerDay());
		assertEquals(0.15, profile.skipRate());
		assertEquals(5, profile.adsListenedPerWeek());
		assertEquals("Mobile", profile.deviceType());
		assertTrue(profile.offlineListening());
		assertEquals("user-123", profile.userId());
	}

	/**
	 * Helper para criar perfil de teste com subscription type customizado.
	 *
	 * @param subscriptionType tipo de assinatura
	 * @return CustomerProfile com mínimo de campos preenchidos
	 */
	private CustomerProfile createProfile(String subscriptionType) {
		return CustomerProfile.builder()
				.subscriptionType(subscriptionType)
				// Campos obrigatórios no domínio: userId, gender e age
				.userId("test-user")
				.gender("Male")
				.age(30)
				// Valores mínimos plausíveis para passar validações adicionais, se houver
				.listeningTime(60.0)
				.songsPlayedPerDay(1)
				.adsListenedPerWeek(0)
				.deviceType("Mobile")
				.offlineListening(false)
				.build();
	}
}

