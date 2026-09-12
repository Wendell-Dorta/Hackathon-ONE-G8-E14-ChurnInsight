-- =============================================================================
-- Script de Teste: Diagnóstico da Receita em Risco
-- =============================================================================

-- 1. Verificar se há dados na tabela
SELECT 
    COUNT(*) as total_registros,
    MIN(created_at) as primeira_predicao,
    MAX(created_at) as ultima_predicao
FROM churn_history;

-- 2. Verificar distribuição por tipo de assinatura
SELECT 
    subscription_type,
    COUNT(*) as quantidade,
    ROUND(AVG(probability), 3) as prob_media,
    ROUND(MIN(probability), 3) as prob_min,
    ROUND(MAX(probability), 3) as prob_max
FROM churn_history
GROUP BY subscription_type
ORDER BY quantidade DESC;

-- 3. Verificar TOP 25% (mesma lógica do backend)
SELECT 
    ranked.subscription_type,
    COUNT(*) as count_top25,
    ROUND(AVG(ranked.probability), 3) as prob_media
FROM (
    SELECT 
        subscription_type,
        probability,
        ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
        COUNT(*) OVER () as total_count
    FROM churn_history
) ranked
WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
GROUP BY ranked.subscription_type
ORDER BY count_top25 DESC;

-- 4. Calcular receita em risco manualmente (para validar)
WITH top25 AS (
    SELECT 
        ranked.subscription_type,
        COUNT(*) as count
    FROM (
        SELECT 
            subscription_type,
            ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
            COUNT(*) OVER () as total_count
        FROM churn_history
    ) ranked
    WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
    GROUP BY ranked.subscription_type
),
plan_values AS (
    SELECT 'Premium' as plan, 23.90 as value
    UNION ALL SELECT 'Family', 40.90
    UNION ALL SELECT 'Duo', 31.90
    UNION ALL SELECT 'Student', 12.90
    UNION ALL SELECT 'Free', 0.0
)
SELECT 
    t.subscription_type,
    t.count as clientes_em_risco,
    COALESCE(p.value, 0) as valor_plano,
    ROUND(t.count * COALESCE(p.value, 0), 2) as receita_em_risco
FROM top25 t
LEFT JOIN plan_values p ON t.subscription_type = p.plan
ORDER BY receita_em_risco DESC;

-- 5. Total de receita em risco (resultado final esperado)
WITH top25 AS (
    SELECT 
        ranked.subscription_type,
        COUNT(*) as count
    FROM (
        SELECT 
            subscription_type,
            ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
            COUNT(*) OVER () as total_count
        FROM churn_history
    ) ranked
    WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
    GROUP BY ranked.subscription_type
),
plan_values AS (
    SELECT 'Premium' as plan, 23.90 as value
    UNION ALL SELECT 'Family', 40.90
    UNION ALL SELECT 'Duo', 31.90
    UNION ALL SELECT 'Student', 12.90
    UNION ALL SELECT 'Free', 0.0
)
SELECT 
    ROUND(SUM(t.count * COALESCE(p.value, 0)), 2) as receita_total_em_risco
FROM top25 t
LEFT JOIN plan_values p ON t.subscription_type = p.plan;

-- 6. Verificar se há valores NULL em subscription_type
SELECT 
    COUNT(*) as registros_sem_subscription_type
FROM churn_history
WHERE subscription_type IS NULL OR subscription_type = '';

-- 7. Verificar distribuição de probabilidades
SELECT 
    CASE 
        WHEN probability < 0.25 THEN '0-25% (Baixo)'
        WHEN probability < 0.50 THEN '25-50% (Médio)'
        WHEN probability < 0.75 THEN '50-75% (Alto)'
        ELSE '75-100% (Muito Alto)'
    END as faixa_risco,
    COUNT(*) as quantidade,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM churn_history), 2) as percentual
FROM churn_history
GROUP BY 
    CASE 
        WHEN probability < 0.25 THEN '0-25% (Baixo)'
        WHEN probability < 0.50 THEN '25-50% (Médio)'
        WHEN probability < 0.75 THEN '50-75% (Alto)'
        ELSE '75-100% (Muito Alto)'
    END
ORDER BY faixa_risco;

-- 8. Verificar os 10 clientes com maior probabilidade de churn
SELECT 
    user_id,
    subscription_type,
    ROUND(probability, 3) as probabilidade,
    churn_status,
    created_at
FROM churn_history
ORDER BY probability DESC
LIMIT 10;
