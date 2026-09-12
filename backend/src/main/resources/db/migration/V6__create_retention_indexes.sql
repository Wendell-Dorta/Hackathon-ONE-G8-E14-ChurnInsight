-- =============================================================================
-- Migração: V6__create_retention_indexes.sql
-- Descrição: Índices para consultas operacionais de retenção
-- =============================================================================

CREATE INDEX idx_retention_action_client ON retention_action(client_id);
CREATE INDEX idx_retention_action_status ON retention_action(status);
CREATE INDEX idx_retention_action_created_at ON retention_action(created_at DESC);
CREATE INDEX idx_retention_action_executed_at ON retention_action(executed_at DESC);
CREATE INDEX idx_retention_action_owner ON retention_action(owner);

CREATE INDEX idx_retention_outcome_observed_at ON retention_outcome(observed_at DESC);
CREATE INDEX idx_retention_outcome_retained ON retention_outcome(retained);
