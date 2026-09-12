package com.hackathon.databeats.churninsight.infra.adapter.input.web.controller;

import com.hackathon.databeats.churninsight.application.dto.ApiContract;
import com.hackathon.databeats.churninsight.application.port.input.ApiContractQueryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller REST para consultar o contrato da API de predição.
 * Os dados são carregados do arquivo contrato_api.json gerado pelo modelo ONNX.
 */
@RestController
@Slf4j
@Tag(name = "API Contract", description = "APIs para consultar o contrato e especificação da API de predição")
@RequestMapping(value = "/contract", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApiContractController {

    private final ApiContractQueryUseCase apiContractQueryUseCase;

    public ApiContractController(ApiContractQueryUseCase apiContractQueryUseCase) {
        this.apiContractQueryUseCase = apiContractQueryUseCase;
    }

    @GetMapping
    @Operation(summary = "Obter contrato completo da API",
            description = "Retorna o contrato completo da API de predição, incluindo payload de exemplo e resposta esperada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contrato retornado com sucesso")
    })
    public ResponseEntity<ApiContract> getContract() {
        log.debug("Obtendo contrato completo da API");
        return ResponseEntity.ok(apiContractQueryUseCase.getContract());
    }

    @GetMapping("/sample-payload")
    @Operation(summary = "Obter payload de exemplo",
            description = "Retorna um payload de exemplo para testes de predição")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payload de exemplo retornado com sucesso")
    })
    public ResponseEntity<ApiContract.PayloadInput> getSamplePayload() {
        log.debug("Obtendo payload de exemplo");
        return ResponseEntity.ok(apiContractQueryUseCase.getSamplePayload());
    }

    @GetMapping("/expected-response")
    @Operation(summary = "Obter resposta esperada",
            description = "Retorna a resposta esperada para o payload de exemplo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resposta esperada retornada com sucesso")
    })
    public ResponseEntity<ApiContract.ExpectedResponse> getExpectedResponse() {
        log.debug("Obtendo resposta esperada");
        return ResponseEntity.ok(apiContractQueryUseCase.getExpectedResponse());
    }

    @GetMapping("/model-info")
    @Operation(summary = "Obter informações do modelo",
            description = "Retorna os metadados do modelo de predição")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informações do modelo retornadas com sucesso")
    })
    public ResponseEntity<ApiContract.Metadata> getModelInfo() {
        log.debug("Obtendo informações do modelo");
        return ResponseEntity.ok(apiContractQueryUseCase.getModelInfo());
    }
}

