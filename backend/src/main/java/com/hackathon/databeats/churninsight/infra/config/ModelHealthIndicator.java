package com.hackathon.databeats.churninsight.infra.config;

import com.hackathon.databeats.churninsight.application.port.output.InferencePort;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Indicator para monitorar o status do modelo de ML.
 * Usa InferencePort para verificar se o modelo está carregado.
 */
@Component("model")
public class ModelHealthIndicator implements HealthIndicator {
    private final InferencePort inferencePort;

    public ModelHealthIndicator(InferencePort inferencePort) {
        this.inferencePort = inferencePort;
    }

    @Override
    public Health health() {
        try {
            boolean isLoaded = inferencePort.isModelLoaded();

            if (isLoaded) {
                return Health.up()
                        .withDetail("status", "Modelo ONNX carregado com sucesso")
                        .withDetail("session", "Ativa")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Modelo ONNX não carregado")
                        .withDetail("session", "Inativa")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Erro ao verificar modelo")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}