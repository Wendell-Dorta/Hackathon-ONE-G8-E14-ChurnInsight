package com.hackathon.databeats.churninsight.domain.rules;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de domínio que implementa regras de negócio para cálculo de features de churn.
 *
 * <p>Centraliza a lógica de <b>Engenharia de Features (Feature Engineering)</b> que alimenta
 * o modelo de Machine Learning. Todas as features calculadas aqui são derivadas das features
 * originais do cliente e representam insights de negócio sobre comportamento de churn.</p>
 *
 * <h3>Features Calculadas:</h3>
 * <table border="1">
 *   <tr><th>Feature</th><th>Fórmula</th><th>Significado de Negócio</th></tr>
 *   <tr>
 *     <td>frustration_index</td>
 *     <td>skip_rate × (ads_per_week + 1)</td>
 *     <td>Mede frustração do usuário com anúncios e conteúdo irrelevante</td>
 *   </tr>
 *   <tr>
 *     <td>ad_intensity</td>
 *     <td>ads_per_week / ((songs_per_day × 7) + 1)</td>
 *     <td>Proporção de exposição a anúncios vs consumo musical</td>
 *   </tr>
 *   <tr>
 *     <td>songs_per_minute</td>
 *     <td>songs_per_day / (listening_time + 1)</td>
 *     <td>Velocidade de engajamento (músicas curtas vs longas)</td>
 *   </tr>
 *   <tr>
 *     <td>is_heavy_user</td>
 *     <td>listening_time > 450 AND skip_rate < 0.2</td>
 *     <td>Identifica usuários altamente engajados (baixo risco de churn)</td>
 *   </tr>
 *   <tr>
 *     <td>premium_no_offline</td>
 *     <td>subscription_type ≠ Free AND !offline_listening</td>
 *     <td>Subutilização de recurso Premium (potencial insatisfação)</td>
 *   </tr>
 * </table>
 *
 * <h3>Importância para o Modelo ONNX:</h3>
 * <p>Essas features foram selecionadas durante o treinamento do modelo por apresentarem
 * alta correlação com eventos de churn no dataset histórico. O modelo espera receber
 * exatamente essas 5 features calculadas além das 11 features originais.</p>
 *
 * <h3>Thread Safety:</h3>
 * <p>Este serviço é <b>stateless</b> e <b>thread-safe</b>. Todos os métodos são estáticos
 * e utilizam apenas os parâmetros de entrada, sem estado compartilhado.</p>
 *
 * <h3>Exemplo de Uso:</h3>
 * <pre>{@code
 * CustomerProfile profile = CustomerProfile.builder()
 *     .skipRate(0.4)
 *     .adsListenedPerWeek(10)
 *     .listeningTime(300.0)
 *     .songsPlayedPerDay(20)
 *     .subscriptionType("Premium")
 *     .offlineListening(false)
 *     .build();
 *
 * Map<String, Object> features = ChurnBusinessRules.calculateEngineeredFeatures(profile);
 * // features: {frustration_index=4.4, ad_intensity=0.07, songs_per_minute=0.066,
 * //            is_heavy_user=false, premium_no_offline=true}
 * }</pre>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 * @see com.hackathon.databeats.churninsight.infra.adapter.output.inference.OnnxRuntimeAdapter
 */
public final class ChurnBusinessRules {

    // =========================================================================
    // CONSTANTES DE NEGÓCIO (Thresholds definidos pelo time de Data Science)
    // =========================================================================

    /**
     * Threshold mínimo de tempo de escuta para classificar como heavy user.
     * <p>Valor em minutos. Usuários acima desse threshold são considerados altamente engajados.</p>
     */
    private static final double HEAVY_USER_LISTENING_THRESHOLD = 450.0;

    /**
     * Threshold máximo de taxa de skip para classificar como heavy user.
     * <p>Heavy users tipicamente pulam menos de 20% das músicas.</p>
     */
    private static final double HEAVY_USER_SKIP_THRESHOLD = 0.2;

    /**
     * Tipo de assinatura gratuita.
     * <p>Usado para identificar usuários que não pagam pelo serviço.</p>
     */
    private static final String FREE_SUBSCRIPTION = "Free";

    // =========================================================================
    // CONSTRUTOR PRIVADO (Utility Class)
    // =========================================================================

    /**
     * Construtor privado para impedir instanciação.
     * <p>Esta é uma classe utilitária com métodos estáticos apenas.</p>
     */
    private ChurnBusinessRules() {
        throw new UnsupportedOperationException("Utility class - não instanciável");
    }

    // =========================================================================
    // MÉTODO PRINCIPAL
    // =========================================================================

    /**
     * Calcula as 5 features de engenharia derivadas do perfil do cliente.
     *
     * <p>Essas features são input obrigatório para o modelo ONNX e representam
     * insights de negócio extraídos das features brutas do cliente.</p>
     *
     * <h3>Tratamento de Valores Nulos:</h3>
     * <p>Campos numéricos nulos são tratados como zero (0) para garantir
     * estabilidade do cálculo. Isso é intencional para:
     * <ul>
     *   <li>Evitar NullPointerException em produção</li>
     *   <li>Manter consistência com o tratamento feito no treinamento do modelo</li>
     *   <li>Permitir predições mesmo com dados incompletos (degradação graceful)</li>
     * </ul>
     *
     * @param profile perfil do cliente com features originais
     * @return mapa com as 5 features calculadas (chaves: snake_case para compatibilidade ONNX)
     * @throws NullPointerException se profile for null
     *
     * @see #calculateFrustrationIndex(CustomerProfile)
     * @see #calculateAdIntensity(CustomerProfile)
     * @see #calculateSongsPerMinute(CustomerProfile)
     * @see #isHeavyUser(CustomerProfile)
     * @see #isPremiumNoOffline(CustomerProfile)
     */
    public static Map<String, Object> calculateEngineeredFeatures(CustomerProfile profile) {
        Map<String, Object> features = new HashMap<>(5);

        // Feature 1: Índice de frustração (correlação com anúncios e skips)
        features.put("frustration_index", calculateFrustrationIndex(profile));

        // Feature 2: Intensidade de anúncios (proporção ads/consumo)
        features.put("ad_intensity", calculateAdIntensity(profile));

        // Feature 3: Músicas por minuto (velocidade de engajamento)
        features.put("songs_per_minute", calculateSongsPerMinute(profile));

        // Feature 4: Flag de usuário intenso
        features.put("is_heavy_user", isHeavyUser(profile));

        // Feature 5: Flag de Premium sem uso do offline
        features.put("premium_no_offline", isPremiumNoOffline(profile));

        return features;
    }

    // =========================================================================
    // CÁLCULOS DE FEATURES INDIVIDUAIS
    // =========================================================================

    /**
     * Calcula o índice de frustração do usuário.
     *
     * <p><b>Fórmula:</b> {@code skip_rate × (ads_listened_per_week + 1)}</p>
     *
     * <p><b>Interpretação:</b></p>
     * <ul>
     *   <li><b>Baixo (< 1.0):</b> Usuário satisfeito</li>
     *   <li><b>Médio (1.0 - 3.0):</b> Alguma insatisfação</li>
     *   <li><b>Alto (> 3.0):</b> Alta frustração, risco de churn</li>
     * </ul>
     *
     * <p><b>Lógica de Negócio:</b> Usuários que pulam muitas músicas E são expostos
     * a muitos anúncios têm experiência degradada, aumentando chance de cancelamento.</p>
     *
     * @param profile perfil do cliente
     * @return valor do índice de frustração (≥ 0)
     */
    private static double calculateFrustrationIndex(CustomerProfile profile) {
        double skipRate = safeDouble(profile.skipRate());
        int adsPerWeek = safeInt(profile.adsListenedPerWeek());

        // +1 no ads evita que frustration seja 0 quando não há ads
        return skipRate * (adsPerWeek + 1);
    }

    /**
     * Calcula a intensidade de exposição a anúncios.
     *
     * <p><b>Fórmula:</b> {@code ads_per_week / ((songs_per_day × 7) + 1)}</p>
     *
     * <p><b>Interpretação:</b></p>
     * <ul>
     *   <li><b>Baixo (< 0.05):</b> Poucos anúncios por música</li>
     *   <li><b>Médio (0.05 - 0.15):</b> Proporção típica</li>
     *   <li><b>Alto (> 0.15):</b> Muitos anúncios, experiência degradada</li>
     * </ul>
     *
     * <p><b>Lógica de Negócio:</b> Alta proporção de anúncios vs músicas indica
     * experiência ruim para usuários Free, incentivando upgrade ou churn.</p>
     *
     * @param profile perfil do cliente
     * @return valor da intensidade de anúncios (0.0 - 1.0 tipicamente)
     */
    private static double calculateAdIntensity(CustomerProfile profile) {
        int adsPerWeek = safeInt(profile.adsListenedPerWeek());
        int songsPerDay = safeInt(profile.songsPlayedPerDay());

        // Total de músicas por semana (songs_per_day * 7 dias)
        // +1 evita divisão por zero
        return adsPerWeek / ((songsPerDay * 7.0) + 1);
    }

    /**
     * Calcula a taxa de músicas por minuto de escuta.
     *
     * <p><b>Fórmula:</b> {@code songs_per_day / (listening_time + 1)}</p>
     *
     * <p><b>Interpretação:</b></p>
     * <ul>
     *   <li><b>Baixo (< 0.1):</b> Músicas longas, podcasts, álbuns completos</li>
     *   <li><b>Médio (0.1 - 0.3):</b> Uso típico</li>
     *   <li><b>Alto (> 0.3):</b> Músicas curtas, muitos skips</li>
     * </ul>
     *
     * <p><b>Lógica de Negócio:</b> Valores muito altos podem indicar insatisfação
     * (usuário não encontra o que quer). Valores muito baixos podem indicar uso
     * de recursos premium (podcasts, audiobooks).</p>
     *
     * @param profile perfil do cliente
     * @return taxa de músicas por minuto (≥ 0)
     */
    private static double calculateSongsPerMinute(CustomerProfile profile) {
        int songsPerDay = safeInt(profile.songsPlayedPerDay());
        double listeningTime = safeDouble(profile.listeningTime());

        // +1 evita divisão por zero quando listeningTime = 0
        return songsPerDay / (listeningTime + 1);
    }

    /**
     * Determina se o usuário é classificado como "heavy user" (usuário intenso).
     *
     * <p><b>Critério:</b> {@code listening_time > 450 AND skip_rate < 0.2}</p>
     *
     * <p><b>Interpretação:</b></p>
     * <ul>
     *   <li><b>true:</b> Usuário altamente engajado com o serviço</li>
     *   <li><b>false:</b> Usuário casual ou insatisfeito</li>
     * </ul>
     *
     * <p><b>Lógica de Negócio:</b> Heavy users são o segmento mais valioso e
     * tipicamente têm menor propensão a churn. Representam clientes que
     * realmente usam e valorizam o serviço.</p>
     *
     * @param profile perfil do cliente
     * @return true se usuário é heavy user, false caso contrário
     */
    private static boolean isHeavyUser(CustomerProfile profile) {
        double listeningTime = safeDouble(profile.listeningTime());
        double skipRate = safeDouble(profile.skipRate());

        return listeningTime > HEAVY_USER_LISTENING_THRESHOLD && skipRate < HEAVY_USER_SKIP_THRESHOLD;
    }

    /**
     * Determina se o usuário é Premium mas não utiliza o recurso offline.
     *
     * <p><b>Critério:</b> {@code subscription_type ≠ 'Free' AND !offline_listening}</p>
     *
     * <p><b>Interpretação:</b></p>
     * <ul>
     *   <li><b>true:</b> Cliente Premium subutilizando recursos</li>
     *   <li><b>false:</b> Cliente Free ou Premium usando offline</li>
     * </ul>
     *
     * <p><b>Lógica de Negócio:</b> Usuários Premium que não usam download offline
     * podem não estar percebendo valor suficiente no plano pago, indicando
     * risco de downgrade ou cancelamento. Oportunidade para educação sobre features.</p>
     *
     * @param profile perfil do cliente
     * @return true se é Premium sem usar offline, false caso contrário
     */
    private static boolean isPremiumNoOffline(CustomerProfile profile) {
        String subscriptionType = profile.subscriptionType();
        Boolean offlineListening = profile.offlineListening();

        // Verifica se NÃO é Free (ou seja, é pagante)
        boolean isPremium = subscriptionType != null && !FREE_SUBSCRIPTION.equalsIgnoreCase(subscriptionType);

        // Verifica se NÃO usa offline
        boolean noOffline = offlineListening == null || !offlineListening;

        return isPremium && noOffline;
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE NULL-SAFETY
    // =========================================================================

    /**
     * Converte Double nullable para double primitivo (null = 0.0).
     *
     * @param value valor Double ou null
     * @return valor primitivo double (0.0 se null)
     */
    private static double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    /**
     * Converte Integer nullable para int primitivo (null = 0).
     *
     * @param value valor Integer ou null
     * @return valor primitivo int (0 se null)
     */
    private static int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}

