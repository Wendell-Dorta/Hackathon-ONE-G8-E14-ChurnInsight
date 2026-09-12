package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.adapter;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase.ClientStatistics;
import com.hackathon.databeats.churninsight.application.port.output.ClientPredictionOutputPort;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper.ClientPredictionMapper;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository.ClientPredictionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component // <--- ESTA ANOTAÇÃO É CRÍTICA! Ela diz ao Spring para carregar essa classe.
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientPredictionJpaAdapter implements ClientPredictionOutputPort {

    private final ClientPredictionRepository repository;
    private final ClientPredictionMapper mapper;

    @Override
    public Optional<ClientPrediction> findById(String clientId) {
        return repository.findById(Objects.requireNonNull(clientId, "clientId is required")).map(mapper::toDomain);
    }

    @Override
    public List<ClientPrediction> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ClientPrediction> findHighRiskClients(double threshold, int page, int size) {
        return repository.findByProbabilityGreaterThanEqual(threshold, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ClientPrediction> findByChurnPrediction(boolean willChurn, int page, int size) {
        String status = willChurn ? "WILL_CHURN" : "WILL_STAY";
        return repository.findByPrediction(status, PageRequest.of(page, size))
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long countAll() {
        return repository.count();
    }

    @Override
    public ClientStatistics getStatistics() {
        if (repository.count() == 0) {
            return new ClientStatistics(0, 0, 0, 0.0, 0.0, 0.0);
        }
        return repository.calculateClientStatistics();
    }
}