package com.hackathon.databeats.churninsight.domain.rules;

import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Serviço de domínio para geração de diagnósticos explicativos de predições de churn.
 *
 * <p>Implementa Explicabilidade de IA (XAI) para tornar as predições do modelo ONNX
 * compreensíveis para times de negócio.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public final class ChurnDiagnosisService {

    private static final int HIGH_ADS_THRESHOLD = 15;
    private static final double HIGH_SKIP_RATE_THRESHOLD = 0.35;
    private static final double LOW_ENGAGEMENT_THRESHOLD = 100.0;
    private static final double HEAVY_USER_THRESHOLD = 450.0;

    private static final String RISK_HIGH_ADS = "Anúncios por Semana";
    private static final String RISK_HIGH_SKIP = "Taxa de Pulos Elevada";
    private static final String RISK_LOW_ENGAGEMENT = "Baixo Engajamento";
    private static final String RISK_PREMIUM_NO_OFFLINE = "Subutilização Premium";
    private static final String RISK_DEFAULT = "Perfil de Risco Moderado";

    private static final String RETENTION_OFFLINE = "Uso Offline";
    private static final String RETENTION_HEAVY_USER = "Alto Engajamento";
    private static final String RETENTION_PREMIUM = "Assinatura Premium";
    private static final String RETENTION_LOW_ADS = "Baixa Exposição a Anúncios";
    private static final String RETENTION_DEFAULT = "Uso Regular da Plataforma";

    private ChurnDiagnosisService() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gera diagnóstico explicativo para uma predição de churn.
     *
     * @param profile perfil do cliente
     * @param probability probabilidade de churn (0.0 a 1.0)
     * @param threshold threshold do modelo
     * @return mapa com diagnóstico
     */
    public static Map<String, String> gerarDiagnostico(
            CustomerProfile profile,
            double probability,
            double threshold) {

        Map<String, String> diagnostico = new HashMap<>(3);
        boolean altoRisco = probability >= threshold;

        String riskFactor = identificarFatorRisco(profile, altoRisco);
        String retentionFactor = identificarFatorRetencao(profile);
        String suggestedAction = gerarAcaoSugerida(profile, riskFactor, probability, threshold);

        diagnostico.put("primary_risk_factor", riskFactor);
        diagnostico.put("primary_retention_factor", retentionFactor);
        diagnostico.put("suggested_action", suggestedAction);

        return diagnostico;
    }

    private static String identificarFatorRisco(CustomerProfile profile, boolean altoRisco) {
        if (!altoRisco) return RISK_DEFAULT;

        Integer adsPerWeek = profile.adsListenedPerWeek();
        if (adsPerWeek != null && adsPerWeek > HIGH_ADS_THRESHOLD) {
            return RISK_HIGH_ADS;
        }

        Double skipRate = profile.skipRate();
        if (skipRate != null && skipRate > HIGH_SKIP_RATE_THRESHOLD) {
            return RISK_HIGH_SKIP;
        }

        Double listeningTime = profile.listeningTime();
        if (listeningTime != null && listeningTime < LOW_ENGAGEMENT_THRESHOLD) {
            return RISK_LOW_ENGAGEMENT;
        }

        if (isPremiumWithoutOffline(profile)) {
            return RISK_PREMIUM_NO_OFFLINE;
        }

        return RISK_DEFAULT;
    }

    private static String identificarFatorRetencao(CustomerProfile profile) {
        Boolean offlineListening = profile.offlineListening();
        if (offlineListening != null && offlineListening) {
            return RETENTION_OFFLINE;
        }

        Double listeningTime = profile.listeningTime();
        Double skipRate = profile.skipRate();
        if (listeningTime != null && listeningTime > HEAVY_USER_THRESHOLD
                && skipRate != null && skipRate < 0.2) {
            return RETENTION_HEAVY_USER;
        }

        String subscriptionType = profile.subscriptionType();
        if (subscriptionType != null && !subscriptionType.equalsIgnoreCase("Free")) {
            return RETENTION_PREMIUM;
        }

        Integer adsPerWeek = profile.adsListenedPerWeek();
        if (adsPerWeek != null && adsPerWeek < 5) {
            return RETENTION_LOW_ADS;
        }

        return RETENTION_DEFAULT;
    }

    private static String gerarAcaoSugerida(
            CustomerProfile profile,
            String riskFactor,
            double probability,
            double threshold) {

        if (probability < threshold) {
            return "Manter relacionamento atual com comunicações periódicas.";
        }

        return switch (riskFactor) {
            case RISK_HIGH_ADS -> gerarAcaoHighAds(profile);
            case RISK_HIGH_SKIP -> "Recalibrar algoritmo de recomendação personalizada e enviar playlist curada.";
            case RISK_LOW_ENGAGEMENT -> "Enviar push notifications com novidades e playlists personalizadas.";
            case RISK_PREMIUM_NO_OFFLINE -> "Educar sobre recurso de download offline via email tutorial.";
            default -> gerarAcaoDefault(probability);
        };
    }

    private static String gerarAcaoHighAds(CustomerProfile profile) {
        String subscriptionType = profile.subscriptionType();
        if (subscriptionType != null && subscriptionType.equalsIgnoreCase("Free")) {
            return "Oferecer período de teste do plano Premium para aliviar interrupções de áudio.";
        }
        return "Revisar frequência de anúncios e considerar oferta de upgrade.";
    }

    private static String gerarAcaoDefault(double probability) {
        if (probability > 0.8) {
            return "Priorizar contato urgente com oferta de retenção personalizada.";
        } else if (probability > 0.6) {
            return "Agendar contato proativo com proposta de valor.";
        }
        return "Monitorar comportamento e enviar pesquisa de satisfação.";
    }

    private static boolean isPremiumWithoutOffline(CustomerProfile profile) {
        String subscriptionType = profile.subscriptionType();
        Boolean offlineListening = profile.offlineListening();
        boolean isPremium = subscriptionType != null && !subscriptionType.equalsIgnoreCase("Free");
        boolean noOffline = offlineListening == null || !offlineListening;
        return isPremium && noOffline;
    }
}
