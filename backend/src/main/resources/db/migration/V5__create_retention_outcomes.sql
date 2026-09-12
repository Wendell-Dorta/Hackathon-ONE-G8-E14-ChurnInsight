-- =============================================================================
-- Migração: V5__create_retention_outcomes.sql
-- Descrição: Cria tabela de resultado de ações de retenção
-- =============================================================================

CREATE TABLE IF NOT EXISTS retention_outcome (
    id CHAR(36) NOT NULL,
    action_id CHAR(36) NOT NULL,
    outcome_type VARCHAR(40) NOT NULL,
    retained BOOLEAN NOT NULL,
    recovered_revenue DOUBLE NOT NULL DEFAULT 0,
    observed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    comment VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_retention_outcome_action FOREIGN KEY (action_id) REFERENCES retention_action(id) ON DELETE CASCADE,
    CONSTRAINT uq_retention_outcome_action UNIQUE (action_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
