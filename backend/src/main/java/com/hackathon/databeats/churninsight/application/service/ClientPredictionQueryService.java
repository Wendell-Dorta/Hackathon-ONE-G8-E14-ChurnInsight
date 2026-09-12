package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase;
import com.hackathon.databeats.churninsight.application.port.output.ClientPredictionOutputPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientPredictionQueryService implements ClientPredictionQueryUseCase {

    // Injeção da Porta de Saída (que será implementada pelo JpaAdapter)
    private final ClientPredictionOutputPort outputPort;

    @Override
    public Optional<ClientPrediction> findByClientId(String clientId) {
        return outputPort.findById(clientId);
    }

    @Override
    public List<ClientPrediction> findAll(int page, int size) {
        // Repassa a paginação para o banco
        return outputPort.findAll(page, size);
    }

    @Override
    public List<ClientPrediction> findHighRiskClients(double thresholdProbability, int page, int size) {
        return outputPort.findHighRiskClients(thresholdProbability, page, size);
    }

    @Override
    public List<ClientPrediction> findClientsWhoWillChurn(int page, int size) {
        // true = status WILL_CHURN
        return outputPort.findByChurnPrediction(true, page, size);
    }

    @Override
    public long getTotalClients() {
        return outputPort.countAll();
    }

    @Override
    public ClientStatistics getStatistics() {
        return outputPort.getStatistics();
    }
}