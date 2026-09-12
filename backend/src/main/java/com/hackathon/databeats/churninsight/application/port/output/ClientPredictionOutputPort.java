package com.hackathon.databeats.churninsight.application.port.output;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase.ClientStatistics;

import java.util.List;
import java.util.Optional;

public interface ClientPredictionOutputPort {
    Optional<ClientPrediction> findById(String clientId);
    List<ClientPrediction> findAll(int page, int size);
    List<ClientPrediction> findHighRiskClients(double threshold, int page, int size);
    List<ClientPrediction> findByChurnPrediction(boolean willChurn, int page, int size);
    long countAll();
    ClientStatistics getStatistics();
}