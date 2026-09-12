-- =============================================================================
-- Migração: V4__create_retention_actions.sql
-- Descrição: Cria tabela de ações operacionais de retenção
-- =============================================================================

CREATE TABLE IF NOT EXISTS retention_action (
    id CHAR(36) NOT NULL,
    client_id CHAR(36) NOT NULL,
    user_id CHAR(36) NULL,
    action_type VARCHAR(40) NOT NULL,
    channel VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    owner VARCHAR(80) NULL,
    scheduled_at TIMESTAMP NULL,
    executed_at TIMESTAMP NULL,
    notes VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_retention_action_client FOREIGN KEY (client_id) REFERENCES churn_history(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
