-- =============================================================================
-- Script: Dados de Teste para Receita em Risco
-- Descrição: Insere dados de exemplo para testar o card de receita em risco
-- =============================================================================

-- ATENÇÃO: Este script é apenas para TESTE/DESENVOLVIMENTO
-- NÃO execute em produção com dados reais!

-- Limpar dados de teste anteriores (opcional)
-- DELETE FROM churn_history WHERE requester_id = 'test-data-generator';

-- Inserir 100 clientes de teste com diferentes probabilidades e planos
-- TOP 25% terá probabilidades entre 0.75 e 1.0

-- 10 clientes Premium com alta probabilidade (TOP 25%)
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
) VALUES
    (UUID(), 'Male', 28, 'BR', 'Premium', 450, 50, 0, 0.65, 'Mobile', true, UUID(), 'WILL_CHURN', 0.95, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 32, 'BR', 'Premium', 380, 45, 0, 0.70, 'Desktop', true, UUID(), 'WILL_CHURN', 0.92, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 25, 'BR', 'Premium', 420, 48, 0, 0.68, 'Mobile', true, UUID(), 'WILL_CHURN', 0.89, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 30, 'BR', 'Premium', 400, 46, 0, 0.72, 'Tablet', true, UUID(), 'WILL_CHURN', 0.87, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 27, 'BR', 'Premium', 390, 44, 0, 0.69, 'Mobile', true, UUID(), 'WILL_CHURN', 0.85, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 29, 'BR', 'Premium', 410, 47, 0, 0.71, 'Desktop', true, UUID(), 'WILL_CHURN', 0.83, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 31, 'BR', 'Premium', 370, 43, 0, 0.73, 'Mobile', true, UUID(), 'WILL_CHURN', 0.81, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 26, 'BR', 'Premium', 360, 42, 0, 0.74, 'Tablet', true, UUID(), 'WILL_CHURN', 0.79, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 33, 'BR', 'Premium', 350, 41, 0, 0.75, 'Mobile', true, UUID(), 'WILL_CHURN', 0.77, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 28, 'BR', 'Premium', 340, 40, 0, 0.76, 'Desktop', true, UUID(), 'WILL_CHURN', 0.76, 'test-data-generator', '127.0.0.1', NOW());

-- 8 clientes Family com alta probabilidade (TOP 25%)
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
) VALUES
    (UUID(), 'Male', 35, 'BR', 'Family', 500, 55, 0, 0.60, 'Smart TV', true, UUID(), 'WILL_CHURN', 0.94, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 38, 'BR', 'Family', 480, 52, 0, 0.62, 'Mobile', true, UUID(), 'WILL_CHURN', 0.91, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 40, 'BR', 'Family', 470, 51, 0, 0.63, 'Desktop', true, UUID(), 'WILL_CHURN', 0.88, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 36, 'BR', 'Family', 460, 50, 0, 0.64, 'Tablet', true, UUID(), 'WILL_CHURN', 0.86, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 37, 'BR', 'Family', 450, 49, 0, 0.65, 'Smart TV', true, UUID(), 'WILL_CHURN', 0.84, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 39, 'BR', 'Family', 440, 48, 0, 0.66, 'Mobile', true, UUID(), 'WILL_CHURN', 0.82, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 34, 'BR', 'Family', 430, 47, 0, 0.67, 'Desktop', true, UUID(), 'WILL_CHURN', 0.80, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 41, 'BR', 'Family', 420, 46, 0, 0.68, 'Tablet', true, UUID(), 'WILL_CHURN', 0.78, 'test-data-generator', '127.0.0.1', NOW());

-- 5 clientes Duo com alta probabilidade (TOP 25%)
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
) VALUES
    (UUID(), 'Male', 24, 'BR', 'Duo', 380, 44, 0, 0.70, 'Mobile', true, UUID(), 'WILL_CHURN', 0.93, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 26, 'BR', 'Duo', 370, 43, 0, 0.71, 'Desktop', true, UUID(), 'WILL_CHURN', 0.90, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 25, 'BR', 'Duo', 360, 42, 0, 0.72, 'Mobile', true, UUID(), 'WILL_CHURN', 0.87, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 27, 'BR', 'Duo', 350, 41, 0, 0.73, 'Tablet', true, UUID(), 'WILL_CHURN', 0.85, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Male', 23, 'BR', 'Duo', 340, 40, 0, 0.74, 'Mobile', true, UUID(), 'WILL_CHURN', 0.83, 'test-data-generator', '127.0.0.1', NOW());

-- 2 clientes Student com alta probabilidade (TOP 25%)
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
) VALUES
    (UUID(), 'Male', 20, 'BR', 'Student', 300, 35, 0, 0.75, 'Mobile', false, UUID(), 'WILL_CHURN', 0.96, 'test-data-generator', '127.0.0.1', NOW()),
    (UUID(), 'Female', 21, 'BR', 'Student', 290, 34, 0, 0.76, 'Desktop', false, UUID(), 'WILL_CHURN', 0.93, 'test-data-generator', '127.0.0.1', NOW());

-- 75 clientes com probabilidade média/baixa (não entram no TOP 25%)
-- 30 Premium
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
)
SELECT 
    UUID(), 
    CASE WHEN RAND() > 0.5 THEN 'Male' ELSE 'Female' END,
    FLOOR(20 + RAND() * 40),
    'BR',
    'Premium',
    FLOOR(200 + RAND() * 300),
    FLOOR(20 + RAND() * 30),
    0,
    ROUND(0.3 + RAND() * 0.3, 2),
    CASE FLOOR(RAND() * 4)
        WHEN 0 THEN 'Mobile'
        WHEN 1 THEN 'Desktop'
        WHEN 2 THEN 'Tablet'
        ELSE 'Smart TV'
    END,
    RAND() > 0.5,
    UUID(),
    CASE WHEN RAND() > 0.5 THEN 'WILL_CHURN' ELSE 'WILL_STAY' END,
    ROUND(0.2 + RAND() * 0.5, 2),
    'test-data-generator',
    '127.0.0.1',
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) t2
LIMIT 30;

-- 25 Family
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
)
SELECT 
    UUID(), 
    CASE WHEN RAND() > 0.5 THEN 'Male' ELSE 'Female' END,
    FLOOR(30 + RAND() * 20),
    'BR',
    'Family',
    FLOOR(250 + RAND() * 300),
    FLOOR(25 + RAND() * 30),
    0,
    ROUND(0.25 + RAND() * 0.35, 2),
    CASE FLOOR(RAND() * 4)
        WHEN 0 THEN 'Mobile'
        WHEN 1 THEN 'Desktop'
        WHEN 2 THEN 'Tablet'
        ELSE 'Smart TV'
    END,
    RAND() > 0.3,
    UUID(),
    CASE WHEN RAND() > 0.6 THEN 'WILL_CHURN' ELSE 'WILL_STAY' END,
    ROUND(0.15 + RAND() * 0.55, 2),
    'test-data-generator',
    '127.0.0.1',
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t2
LIMIT 25;

-- 10 Student
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
)
SELECT 
    UUID(), 
    CASE WHEN RAND() > 0.5 THEN 'Male' ELSE 'Female' END,
    FLOOR(18 + RAND() * 7),
    'BR',
    'Student',
    FLOOR(150 + RAND() * 250),
    FLOOR(15 + RAND() * 25),
    0,
    ROUND(0.3 + RAND() * 0.4, 2),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Mobile'
        WHEN 1 THEN 'Desktop'
        ELSE 'Tablet'
    END,
    RAND() > 0.7,
    UUID(),
    CASE WHEN RAND() > 0.5 THEN 'WILL_CHURN' ELSE 'WILL_STAY' END,
    ROUND(0.1 + RAND() * 0.6, 2),
    'test-data-generator',
    '127.0.0.1',
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2) t2
LIMIT 10;

-- 10 Free
INSERT INTO churn_history (
    id, gender, age, country, subscription_type, listening_time, 
    songs_played_per_day, ads_listened_per_week, skip_rate, device_type, 
    offline_listening, user_id, churn_status, probability, 
    requester_id, request_ip, created_at
)
SELECT 
    UUID(), 
    CASE WHEN RAND() > 0.5 THEN 'Male' ELSE 'Female' END,
    FLOOR(18 + RAND() * 40),
    'BR',
    'Free',
    FLOOR(50 + RAND() * 200),
    FLOOR(10 + RAND() * 20),
    FLOOR(10 + RAND() * 30),
    ROUND(0.4 + RAND() * 0.4, 2),
    CASE FLOOR(RAND() * 3)
        WHEN 0 THEN 'Mobile'
        WHEN 1 THEN 'Desktop'
        ELSE 'Tablet'
    END,
    false,
    UUID(),
    CASE WHEN RAND() > 0.4 THEN 'WILL_CHURN' ELSE 'WILL_STAY' END,
    ROUND(0.2 + RAND() * 0.5, 2),
    'test-data-generator',
    '127.0.0.1',
    NOW()
FROM 
    (SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) t1,
    (SELECT 1 UNION SELECT 2) t2
LIMIT 10;

-- Verificar resultado
SELECT 
    'Total de registros inseridos' as descricao,
    COUNT(*) as valor
FROM churn_history 
WHERE requester_id = 'test-data-generator'

UNION ALL

SELECT 
    'Receita estimada em risco (TOP 25%)' as descricao,
    ROUND(SUM(
        CASE subscription_type
            WHEN 'Premium' THEN 23.90
            WHEN 'Family' THEN 40.90
            WHEN 'Duo' THEN 31.90
            WHEN 'Student' THEN 12.90
            ELSE 0
        END
    ), 2) as valor
FROM (
    SELECT 
        subscription_type,
        ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
        COUNT(*) OVER () as total_count
    FROM churn_history
    WHERE requester_id = 'test-data-generator'
) ranked
WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25);

-- Distribuição do TOP 25%
SELECT 
    subscription_type,
    COUNT(*) as quantidade,
    CASE subscription_type
        WHEN 'Premium' THEN 23.90
        WHEN 'Family' THEN 40.90
        WHEN 'Duo' THEN 31.90
        WHEN 'Student' THEN 12.90
        ELSE 0
    END as valor_plano,
    ROUND(COUNT(*) * CASE subscription_type
        WHEN 'Premium' THEN 23.90
        WHEN 'Family' THEN 40.90
        WHEN 'Duo' THEN 31.90
        WHEN 'Student' THEN 12.90
        ELSE 0
    END, 2) as receita_em_risco
FROM (
    SELECT 
        subscription_type,
        ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
        COUNT(*) OVER () as total_count
    FROM churn_history
    WHERE requester_id = 'test-data-generator'
) ranked
WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
GROUP BY subscription_type
ORDER BY receita_em_risco DESC;
