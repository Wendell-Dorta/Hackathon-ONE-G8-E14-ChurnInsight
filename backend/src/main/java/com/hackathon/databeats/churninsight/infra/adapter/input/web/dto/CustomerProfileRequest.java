package com.hackathon.databeats.churninsight.infra.adapter.input.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import jakarta.validation.constraints.*;

import java.util.Objects;

/**
 * DTO de entrada para requisições de predição de churn.
 *
 * <p>Responsável pela:</p>
 * <ul>
 *   <li>Deserialização JSON com mapeamento de propriedades</li>
 *   <li>Validação automática via JSR-380 (@Valid)</li>
 *   <li>Conversão para modelo de domínio ({@link CustomerProfile})</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public record CustomerProfileRequest(

    /** Identificador único do cliente no sistema externo. */
    @JsonProperty("user_id")
    @NotBlank(message = "O ID do usuário é obrigatório")
    String userId,

    /** Gênero do cliente (Male/Female/Other). */
    @NotBlank(message = "O gênero é obrigatório")
    String gender,

    /** Idade do cliente em anos. */
    @NotNull(message = "A idade é obrigatória")
    @Min(value = 10, message = "Idade mínima permitida: 10 anos")
    @Max(value = 120, message = "Idade máxima permitida: 120 anos")
    Integer age,

    /** Código do país no formato ISO (ex: BR, US, UK). */
    @NotBlank(message = "O país é obrigatório")
    String country,

    /** Tipo de assinatura do cliente (Free/Premium/Student/Family). */
    @JsonProperty("subscription_type")
    @NotBlank(message = "O tipo de assinatura é obrigatório")
    String subscriptionType,

    /** Tempo de escuta mensal em minutos. */
    @JsonProperty("listening_time")
    @NotNull(message = "O tempo de escuta é obrigatório")
    @PositiveOrZero(message = "O tempo de escuta deve ser positivo ou zero")
    Double listeningTime,

    /** Quantidade média de músicas reproduzidas por dia. */
    @JsonProperty("songs_played_per_day")
    @NotNull(message = "A quantidade de músicas por dia é obrigatória")
    @Min(value = 0, message = "A quantidade de músicas não pode ser negativa")
    Integer songsPlayedPerDay,

    /** Taxa de pulo de músicas (0.0 a 1.0). */
    @JsonProperty("skip_rate")
    @NotNull(message = "A taxa de pulo é obrigatória")
    @DecimalMin(value = "0.0", message = "A taxa de pulo deve ser >= 0")
    @DecimalMax(value = "1.0", message = "A taxa de pulo deve ser <= 1")
    Double skipRate,

    /** Quantidade de anúncios ouvidos por semana. */
    @JsonProperty("ads_listened_per_week")
    @NotNull(message = "A quantidade de anúncios por semana é obrigatória")
    @Min(value = 0, message = "A quantidade de anúncios não pode ser negativa")
    Integer adsListenedPerWeek,

    /** Tipo de dispositivo principal (Mobile/Desktop/Tablet/Smart TV). */
    @JsonProperty("device_type")
    @NotBlank(message = "O tipo de dispositivo é obrigatório")
    String deviceType,

    /** Indica se o cliente utiliza o recurso de download offline. */
    @JsonProperty("offline_listening")
    @NotNull(message = "O campo de escuta offline é obrigatório")
    Boolean offlineListening
) {

    /**
     * Converte este DTO para o modelo de domínio.
     *
     * <p>Chamado automaticamente pelo Spring após validação com @Valid.
     * Mapeia todos os campos do DTO para o modelo de domínio imutável.</p>
     *
     * @return instância de {@link CustomerProfile} com os dados validados
     */
    public CustomerProfile toDomain() {
        Objects.requireNonNull(userId);
        return CustomerProfile.builder()
                .userId(this.userId)
                .gender(this.gender)
                .age(this.age)
                .country(this.country)
                .subscriptionType(this.subscriptionType)
                .listeningTime(this.listeningTime)
                .songsPlayedPerDay(this.songsPlayedPerDay)
                .skipRate(this.skipRate)
                .adsListenedPerWeek(this.adsListenedPerWeek)
                .deviceType(this.deviceType)
                .offlineListening(this.offlineListening)
                .build();
    }
}
