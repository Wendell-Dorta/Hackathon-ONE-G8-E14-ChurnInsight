package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.repository;

import com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase.ClientStatistics;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.ClientPredictionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientPredictionRepository extends JpaRepository<ClientPredictionEntity, String> {

    // Busca paginada por probabilidade
    List<ClientPredictionEntity> findByProbabilityGreaterThanEqual(double probability, Pageable pageable);

    // Busca paginada por status (WILL_CHURN ou WILL_STAY)
    List<ClientPredictionEntity> findByPrediction(String prediction, Pageable pageable);

    // SQL Otimizado para Estatísticas (Retorna direto o DTO, economiza memória)
    @Query("""
        SELECT new com.hackathon.databeats.churninsight.application.port.input.ClientPredictionQueryUseCase$ClientStatistics(
            COUNT(c),
            SUM(CASE WHEN c.prediction = 'WILL_CHURN' THEN 1 ELSE 0 END),
            SUM(CASE WHEN c.prediction = 'WILL_STAY' THEN 1 ELSE 0 END),
            COALESCE(AVG(c.probability), 0.0),
            COALESCE(MAX(c.probability), 0.0),
            COALESCE(MIN(c.probability), 0.0)
        ) FROM ClientPredictionEntity c
    """)
    ClientStatistics calculateClientStatistics();
}