-- =============================================================================
-- Migração: V2__add_engineered_features.sql
-- Descrição: Adiciona colunas de features calculadas para análise do modelo
-- Autor: Equipe ChurnInsight
-- Data: 2024-01-02
-- =============================================================================

DROP PROCEDURE IF EXISTS AddColumnIfNotExists;

DELIMITER $$
CREATE PROCEDURE AddColumnIfNotExists(
    IN tableName VARCHAR(255),
    IN colName VARCHAR(255),
    IN colDef VARCHAR(255)
)
BEGIN
    DECLARE colCount INT;

    SELECT COUNT(*)
    INTO colCount
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = tableName
      AND column_name = colName;

    IF colCount = 0 THEN
        SET @s = CONCAT('ALTER TABLE ', tableName, ' ADD COLUMN ', colName, ' ', colDef);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

-- Índice de Frustração: Mede frustração do usuário baseado em skips e anúncios
-- Fórmula: skip_rate * (ads_listened_per_week + 1)
CALL AddColumnIfNotExists('churn_history', 'frustration_index', 'DOUBLE COMMENT ''Índice de frustração calculado''');

-- Intensidade de Anúncios: Proporção de anúncios em relação ao consumo de músicas
-- Fórmula: ads_per_week / ((songs_per_day * 7) + 1)
CALL AddColumnIfNotExists('churn_history', 'ad_intensity', 'DOUBLE COMMENT ''Proporção de exposição a anúncios''');

-- Músicas por Minuto: Métrica de velocidade de engajamento
-- Fórmula: songs_per_day / (listening_time + 1)
CALL AddColumnIfNotExists('churn_history', 'songs_per_minute', 'DOUBLE COMMENT ''Taxa de consumo de músicas''');

-- Flag de Usuário Intenso: Identifica usuários altamente engajados
-- Critério: listening_time > 450 AND skip_rate < 0.2
CALL AddColumnIfNotExists('churn_history', 'is_heavy_user', 'BOOLEAN DEFAULT FALSE COMMENT ''Flag de usuário com alto engajamento''');

-- Premium sem Offline: Identifica usuários premium que não usam recurso offline
-- Critério: subscription_type != Free AND offline_listening = false
CALL AddColumnIfNotExists('churn_history', 'premium_no_offline', 'BOOLEAN DEFAULT FALSE COMMENT ''Premium sem uso do recurso offline''');

DROP PROCEDURE AddColumnIfNotExists;

