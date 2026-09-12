-- =============================================================================
-- Migração: V1__init_churn_history.sql
-- Descrição: Cria a tabela principal para armazenamento do histórico de predições
-- Autor: Equipe ChurnInsight
-- Data: 2024-01-01
-- =============================================================================

CREATE TABLE IF NOT EXISTS churn_history (
    -- Chave Primária (UUID v7 para ordenação temporal)
    id CHAR(36) NOT NULL,

    -- Perfil do Cliente (Features de Entrada)
    gender VARCHAR(20) COMMENT 'Gênero do cliente (Male/Female/Other)',
    age INT COMMENT 'Idade do cliente em anos',
    country VARCHAR(10) COMMENT 'Código do país ISO (BR, US, UK, etc)',
    subscription_type VARCHAR(30) COMMENT 'Tipo de assinatura (Free/Premium/Student/Family)',
    listening_time DOUBLE COMMENT 'Tempo de escuta mensal em minutos',
    songs_played_per_day INT COMMENT 'Média de músicas por dia',
    skip_rate DOUBLE COMMENT 'Taxa de pulo de músicas (0.0 a 1.0)',
    ads_listened_per_week INT COMMENT 'Anúncios ouvidos por semana',
    device_type VARCHAR(30) COMMENT 'Tipo de dispositivo (Mobile/Desktop/Tablet/Smart TV)',
    offline_listening BOOLEAN COMMENT 'Utiliza recurso de download offline',
    user_id CHAR(36) COMMENT 'Identificador externo do usuário',

    -- Saída do Modelo
    churn_status ENUM('WILL_CHURN', 'WILL_STAY') NOT NULL COMMENT 'Resultado da predição',
    probability DOUBLE NOT NULL COMMENT 'Probabilidade de churn (0.0 a 1.0)',

    -- Campos de Auditoria
    requester_id CHAR(36) COMMENT 'ID do solicitante/sistema',
    request_ip VARCHAR(45) COMMENT 'Endereço IP da requisição (IPv4/IPv6)',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Data/hora de criação do registro',

    -- Constraints
    PRIMARY KEY (id),

    -- Validações
    CONSTRAINT chk_probability CHECK (probability >= 0 AND probability <= 1),
    CONSTRAINT chk_skip_rate CHECK (skip_rate >= 0 AND skip_rate <= 1),
    CONSTRAINT chk_age CHECK (age >= 0 AND age <= 150)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Histórico de predições de churn para análise e auditoria';
