package com.hackathon.databeats.churninsight.infra.adapter.output.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidade JPA mínima para representar predições de clientes (usada pelo repository).
 * Criada apenas para compilar o projeto enquanto a implementação completa não estiver presente.
 */
@Entity
@Table(name = "client_prediction")
public class ClientPredictionEntity {

    @Id
    @Column(length = 64)
    private String clientId;

    @Column
    private double probability;

    @Column(length = 20)
    private String prediction;

    public ClientPredictionEntity() {
    }

    public ClientPredictionEntity(String clientId, double probability, String prediction) {
        this.clientId = clientId;
        this.probability = probability;
        this.prediction = prediction;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }
}

