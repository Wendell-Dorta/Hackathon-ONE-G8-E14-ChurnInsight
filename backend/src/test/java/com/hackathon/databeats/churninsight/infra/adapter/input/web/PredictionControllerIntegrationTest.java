package com.hackathon.databeats.churninsight.infra.adapter.input.web;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.application.port.input.BatchProcessingUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictionStatsUseCase;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.infra.adapter.input.web.controller.PredictionController;
import com.hackathon.databeats.churninsight.infra.util.ModelMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração para o PredictionController.
 *
 * <p>Valida:</p>
 * <ul>
 *   <li>Endpoint POST /predict com dados válidos e inválidos</li>
 *   <li>Endpoint POST /stats com retorno de estatísticas</li>
 *   <li>Endpoint GET /health com status da API</li>
 *   <li>Validações HTTP (status codes, headers)</li>
 *   <li>Segurança (autenticação, autorização)</li>
 * </ul>
 *
 * <p><b>Tipo:</b> Teste de integração com MockMvc (sem BD)</p>
 * <p><b>Escopo:</b> Camada de entrada (Controller)</p>
 * <p><b>Dependências:</b> Mocks de use cases</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@WebMvcTest(PredictionController.class)
@ContextConfiguration(classes = {PredictionController.class, PredictionControllerIntegrationTest.TestSecurityConfig.class})
class PredictionControllerIntegrationTest {

	/**
	 * Configuração de segurança simplificada para testes.
	 *
	 * <p>Permite testar os endpoints sem carregar toda a infraestrutura
	 * de segurança Spring, mas mantendo autenticação básica funcional.</p>
	 */
	static class TestSecurityConfig {

		/**
		 * Define chain de filtros de segurança para testes.
		 *
		 * <p>Habilita CSRF disabled, autorização simples, e HTTP Basic auth.</p>
		 *
		 * @param http HttpSecurity builder
		 * @return SecurityFilterChain configurada
		 * @throws Exception se configuração inválida
		 */
		@Bean
		public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
			http
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(auth -> auth
					.requestMatchers("/health").permitAll()
					.anyRequest().authenticated())
				.httpBasic(basic -> basic.realmName("Test"));
			return http.build();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private PredictionStatsUseCase predictionStatsUseCase;

    @MockitoBean
    private PredictChurnUseCase predictChurnUseCase;

	@MockitoBean
	private BatchProcessingUseCase batchProcessingUseCase;

	@MockitoBean
	private ModelMetadata modelMetadata;

    @Test
    @DisplayName("POST /predict deve retornar 200 com dados válidos")
    @WithMockUser(username = "testuser")
    void shouldReturn200WithValidData() throws Exception {
        // Arrange - mock da resposta do use case
        PredictionResult mockResult = PredictionResult.builder()
            .label(ChurnStatus.WILL_STAY)
            .probability(0.25)
            .probabilities(new float[]{0.75f, 0.25f})
            .classProbabilities(Map.of("WILL_STAY", 0.75f, "WILL_CHURN", 0.25f))
            .build();

        when(predictChurnUseCase.predict(any(CustomerProfile.class), anyString(), anyString()))
            .thenReturn(mockResult);

        String requestBody = createValidProfileJson();

        // Act & Assert - executar e validar resposta HTTP
        mockMvc.perform(post("/predict")
                        .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(java.util.Objects.requireNonNull(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prediction").exists())
                .andExpect(jsonPath("$.probability").exists())
                .andExpect(jsonPath("$.ai_diagnosis").exists());
    }

    /**
     * Valida POST /predict retorna 400 com dados inválidos (empty).
     */
    @Test
    @DisplayName("POST /predict deve retornar 400 com dados inválidos")
    @WithMockUser(username = "testuser")
    void shouldReturn400WithInvalidData() throws Exception {
        // Profile vazio - sem campos obrigatórios
        String emptyProfile = "{}";

        mockMvc.perform(post("/predict")
                        .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content(emptyProfile))
                .andExpect(status().isBadRequest());
    }

    /**
     * Valida POST /predict retorna 400 com idade inválida.
     */
    @Test
    @DisplayName("POST /predict deve retornar 400 com idade inválida")
    @WithMockUser(username = "testuser")
    void shouldReturn400WithInvalidAge() throws Exception {
        String requestBody = createProfileWithInvalidAgeJson();

        mockMvc.perform(post("/predict")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(requestBody))
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * Valida POST /predict retorna 400 com skip_rate inválido.
     */
    @Test
    @DisplayName("POST /predict deve retornar 400 com skip_rate inválido")
    @WithMockUser(username = "testuser")
    void shouldReturn400WithInvalidSkipRate() throws Exception {
        String requestBody = createProfileWithInvalidSkipRateJson();

        mockMvc.perform(post("/predict")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(requestBody))
            )
            .andExpect(status().isBadRequest());
    }

    /**
     * Valida POST /stats retorna estatísticas completas com todas as classes.
     */
    @Test
    @DisplayName("POST /stats deve retornar estatísticas completas")
    @WithMockUser(username = "testuser")
    void shouldReturnCompleteStats() throws Exception {
        // Arrange - mock com probabilidades
        PredictionResult mockResult = PredictionResult.builder()
                .label(ChurnStatus.WILL_STAY)
                .probability(0.25)
                .probabilities(new float[]{0.75f, 0.25f})
                .classProbabilities(Map.of("WILL_STAY", 0.75f, "WILL_CHURN", 0.25f))
                .build();

        when(predictionStatsUseCase.predictWithStats(any(CustomerProfile.class), anyString(), anyString()))
            .thenReturn(mockResult);

        String requestBody = createValidProfileJson();

        // Act & Assert - validar resposta com estatísticas
        mockMvc.perform(post("/stats")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(requestBody)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.label").exists())
            .andExpect(jsonPath("$.probability").exists())
            .andExpect(jsonPath("$.class_probabilities").exists());
    }

    /**
     * Valida GET /health retorna status UP com informações do modelo.
     */
    @Test
    @DisplayName("GET /health deve retornar status da API")
    @WithMockUser(username = "testuser")
    void shouldReturnHealthStatus() throws Exception {
        // Arrange - mock indicando modelo saudável
        when(batchProcessingUseCase.isModelHealthy()).thenReturn(true);

        // Act & Assert - validar resposta de health
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.model_status").value("LOADED"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    /**
     * Valida POST /predict retorna 401 (Unauthorized) sem credenciais.
     */
    @Test
    @DisplayName("POST /predict deve retornar 401 sem autenticação")
    void shouldReturn401WithoutAuthentication() throws Exception {
        String requestBody = createValidProfileJson();

        mockMvc.perform(post("/predict")
                .contentType(java.util.Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(java.util.Objects.requireNonNull(requestBody))
            )
            .andExpect(status().isUnauthorized());
    }

    /**
     * Helper para criar JSON de perfil de cliente válido.
     *
     * <p>Contém todos os 11 campos com valores dentro dos ranges validados.</p>
     *
     * @return JSON string com perfil completo e válido
     */
    private String createValidProfileJson() {
        return """
            {
                "user_id": "test-user-integration",
                "gender": "Female",
                "age": 28,
                "country": "US",
                "subscription_type": "Free",
                "listening_time": 320.5,
                "songs_played_per_day": 18,
                "skip_rate": 0.25,
                "ads_listened_per_week": 15,
                "device_type": "Desktop",
                "offline_listening": false
            }
            """;
    }

    /**
     * Helper para criar JSON com idade inválida (< 10).
     *
     * @return JSON string com age=5 (viola @Min(10))
     */
    private String createProfileWithInvalidAgeJson() {
        return """
            {
                "user_id": "test-user-integration",
                "gender": "Female",
                "age": 5,
                "country": "US",
                "subscription_type": "Free",
                "listening_time": 320.5,
                "songs_played_per_day": 18,
                "skip_rate": 0.25,
                "ads_listened_per_week": 15,
                "device_type": "Desktop",
                "offline_listening": false
            }
            """;
    }

    /**
     * Helper para criar JSON com skip_rate inválido (> 1.0).
     *
     * @return JSON string com skip_rate=1.5 (viola @DecimalMax(1.0))
     */
    private String createProfileWithInvalidSkipRateJson() {
        return """
            {
                "user_id": "test-user-integration",
                "gender": "Female",
                "age": 28,
                "country": "US",
                "subscription_type": "Free",
                "listening_time": 320.5,
                "songs_played_per_day": 18,
                "skip_rate": 1.5,
                "ads_listened_per_week": 15,
                "device_type": "Desktop",
                "offline_listening": false
            }
            """;
    }
}
