-- =============================================================================
-- Migração: V3__add_search_indexes.sql
-- Descrição: Cria índices otimizados para consultas paginadas e filtros
-- Autor: Equipe ChurnInsight
-- Data: 2024-01-03
-- Performance: Projetado para tabelas com milhões de registros
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Índices de Busca Principal
-- -----------------------------------------------------------------------------

-- Índice de status: filtro mais utilizado
CREATE INDEX idx_churn_status ON churn_history(churn_status);

-- Índice temporal: paginação e consultas por data
CREATE INDEX idx_created_at ON churn_history(created_at DESC);

-- Índice composto: Status + Data (cobre padrão de consulta mais comum)
CREATE INDEX idx_status_date ON churn_history(churn_status, created_at DESC);

-- Índice de probabilidade: consultas e ordenação por risco
CREATE INDEX idx_probability ON churn_history(probability);

-- -----------------------------------------------------------------------------
-- Índices de Filtros Demográficos
-- -----------------------------------------------------------------------------

-- Filtro por gênero
CREATE INDEX idx_gender ON churn_history(gender);

-- Filtro por tipo de assinatura
CREATE INDEX idx_subscription_type ON churn_history(subscription_type);

-- Filtro por país
CREATE INDEX idx_country ON churn_history(country);

-- Consultas por faixa etária
CREATE INDEX idx_age ON churn_history(age);

-- Busca por usuário
CREATE INDEX idx_user_id ON churn_history(user_id);

-- Índice composto demográfico (cobre consultas multi-filtro)
CREATE INDEX idx_demographics ON churn_history(gender, age, country);

-- -----------------------------------------------------------------------------
-- Índices Baseados em Features
-- -----------------------------------------------------------------------------

-- Clientes de alto risco (churn com alta probabilidade)
CREATE INDEX idx_high_risk ON churn_history(churn_status, probability DESC);

-- Identificação de usuários intensos
CREATE INDEX idx_heavy_user ON churn_history(is_heavy_user);

-- Análise de frustração
CREATE INDEX idx_frustration ON churn_history(frustration_index);

-- Análise por tipo de dispositivo
CREATE INDEX idx_device_type ON churn_history(device_type);

