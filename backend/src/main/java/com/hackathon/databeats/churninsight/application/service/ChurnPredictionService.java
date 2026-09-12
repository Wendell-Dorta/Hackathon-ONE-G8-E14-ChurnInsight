package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.PredictionResult;
import com.hackathon.databeats.churninsight.application.port.input.PredictChurnUseCase;
import com.hackathon.databeats.churninsight.application.port.input.PredictionStatsUseCase;
import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import com.hackathon.databeats.churninsight.application.port.output.ModelMetadataPort;
import com.hackathon.databeats.churninsight.application.port.output.SaveHistoryPort;
import com.hackathon.databeats.churninsight.domain.enums.ChurnStatus;
import com.hackathon.databeats.churninsight.domain.exception.PredictionException;
import com.hackathon.databeats.churninsight.domain.model.CustomerProfile;
import com.hackathon.databeats.churninsight.domain.model.PredictionHistory;
import com.hackathon.databeats.churninsight.domain.rules.ChurnBusinessRules;
import com.hackathon.databeats.churninsight.infra.util.UUIDv7;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Serviço de aplicação para predição de churn.
 *
 * <p>Orquestra o fluxo de predição utilizando regras de domínio e ports de saída.
 * Implementa os casos de uso definidos nas interfaces de entrada.</p>
 *
 * <p>Segue o padrão da Arquitetura Hexagonal, dependendo apenas de interfaces (ports)
 * e não de implementações concretas de infraestrutura.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChurnPredictionService implements PredictChurnUseCase, PredictionStatsUseCase {

    private final SaveHistoryPort saveHistoryPort;
    private final InferencePort inferencePort;
    private final ModelMetadataPort modelMetadataPort;

    /**
     * {@inheritDoc}
     *
     * <p>Executa a predição e persiste o resultado no histórico para auditoria.</p>
     */
    @Override
    public PredictionResult predict(CustomerProfile profile, String requesterId, String requestIp) {
        try {
            Map<String, Object> featuresCalculadas = ChurnBusinessRules.calculateEngineeredFeatures(profile);
            float[] predicao = inferencePort.predict(profile, featuresCalculadas);
            double probabilidadeChurn = predicao[1];
            double probabilidadePermanencia = predicao[0];

            ChurnStatus status = determinarStatus(probabilidadeChurn);

            PredictionHistory historico = PredictionHistory.fromPrediction(
                    UUIDv7.randomUUIDString(),
                    profile,
                    status,
                    probabilidadeChurn,
                    featuresCalculadas,
                    requesterId,
                    requestIp
            );

            saveHistoryPort.save(historico);

            log.info("Predição salva - UserId: {} | Status: {} | Prob: {}",
                    profile.userId(), status, String.format("%.4f", probabilidadeChurn));

            Map<String, Float> probabilidadesPorClasse = Map.of(
                    ChurnStatus.WILL_CHURN.name(), (float) probabilidadeChurn,
                    ChurnStatus.WILL_STAY.name(), (float) probabilidadePermanencia
            );

            return PredictionResult.builder()
                    .label(status)
                    .probability(probabilidadeChurn)
                    .probabilities(predicao)
                    .classProbabilities(probabilidadesPorClasse)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao executar predição: {}", e.getMessage());
            throw new PredictionException("Falha na execução da predição: " + e.getMessage());
        }
    }
    @Override
    public PredictionResult predictWithStats(CustomerProfile profile, String requesterId, String requestIp) {
        try {
            Map<String, Object> featuresCalculadas = ChurnBusinessRules.calculateEngineeredFeatures(profile);
            float[] predicao = inferencePort.predict(profile, featuresCalculadas);

            double probPermanencia = predicao[0];
            double probChurn = predicao[1];

            ChurnStatus status = determinarStatus(probChurn);

            Map<String, Float> probabilidadesPorClasse = Map.of(
                    ChurnStatus.WILL_CHURN.name(), (float) probChurn,
                    ChurnStatus.WILL_STAY.name(), (float) probPermanencia
            );

            log.info("Predição - UserId: {} | Status: {} | Prob: {}",
                    profile.userId(), status, String.format("%.4f", probChurn));

            return PredictionResult.builder()
                    .label(status)
                    .probability(probChurn)
                    .probabilities(predicao)
                    .classProbabilities(probabilidadesPorClasse)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao executar predição com estatísticas: {}", e.getMessage());
            throw new PredictionException("Falha na execução da predição: " + e.getMessage());
        }
    }

    /**
     * Determina o status de churn com base na probabilidade e threshold do modelo.
     */
    private ChurnStatus determinarStatus(double probabilidadeChurn) {
        return probabilidadeChurn >= modelMetadataPort.getThresholdOtimo()
                ? ChurnStatus.WILL_CHURN
                : ChurnStatus.WILL_STAY;
    }
}