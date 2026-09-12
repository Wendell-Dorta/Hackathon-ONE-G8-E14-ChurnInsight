package com.hackathon.databeats.churninsight.application.service;

import com.hackathon.databeats.churninsight.application.dto.ApiContract;
import com.hackathon.databeats.churninsight.application.port.input.ApiContractQueryUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para consultar o contrato da API de predição carregado do contrato_api.json.
 */
@Slf4j
@Service
public class ApiContractQueryService implements ApiContractQueryUseCase {

    private final ApiContract apiContract;

    public ApiContractQueryService(ApiContract apiContract) {
        this.apiContract = apiContract;
        log.info("✅ ApiContractQueryService inicializado - Modelo: {} v{}",
                apiContract.metadata().modelName(),
                apiContract.metadata().modelVersion());
    }

    @Override
    public ApiContract getContract() {
        return apiContract;
    }

    @Override
    public ApiContract.PayloadInput getSamplePayload() {
        return apiContract.getPayloadInput();
    }

    @Override
    public ApiContract.ExpectedResponse getExpectedResponse() {
        return apiContract.getExpectedResponse();
    }

    @Override
    public ApiContract.Metadata getModelInfo() {
        return apiContract.getMetadata();
    }
}

