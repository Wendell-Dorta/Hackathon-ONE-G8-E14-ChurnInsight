package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.mapper;

import com.hackathon.databeats.churninsight.application.dto.ClientPrediction;
import com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity.ClientPredictionEntity;
import org.springframework.stereotype.Component;

/**
 * Converte Entidades do Banco (JPA) para DTOs de Domínio e vice-versa.
 * Implementação manual para evitar dependências extras como MapStruct se não configurado.
 */
@Component
public class ClientPredictionMapper {

    public ClientPrediction toDomain(ClientPredictionEntity entity) {
        if (entity == null) return null;

        return new ClientPrediction(
                entity.getClientId(),
                entity.getProbability(),
                entity.getPrediction(),
                null,
                null
        );
    }
}