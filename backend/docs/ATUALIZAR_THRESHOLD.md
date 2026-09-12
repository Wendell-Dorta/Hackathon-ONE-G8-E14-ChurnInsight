# 🎯 Como Atualizar o Threshold do Modelo

## 📊 Situação Atual

**Arquivo**: `src/main/resources/metadata.json`

```json
{
    "threshold_otimo": 0.262755,  ← VALOR ANTIGO
    "auc_roc": 0.544,
    "accuracy": 0.6488
}
```

---

## 🔄 Passo a Passo

### 1. Pegar o Threshold do Colab

No Google Colab, após executar a CÉLULA 7, você viu algo assim:

```
📊 RESULTADOS
============================================================
AUC-ROC:   0.7834 (78.34%)
Threshold: 0.4521  ← COPIE ESTE VALOR
```

**Copie o valor do Threshold** (exemplo: 0.4521)

---

### 2. Atualizar metadata.json

Abra o arquivo `src/main/resources/metadata.json` e atualize:

```json
{
    "name": "Spotify Churn Model XGBoost",
    "version": "2.0",
    "model_type": "XGBoost",
    "accuracy": 0.7834,
    "recall": 0.7123,
    "precision": 0.6845,
    "f1_score": 0.6981,
    "auc_roc": 0.7834,
    "threshold_otimo": 0.4521,  ← ATUALIZAR COM SEU VALOR
    "numeric_features": [
        "age",
        "listening_time",
        "songs_played_per_day",
        "skip_rate",
        "ads_listened_per_week",
        "offline_listening",
        "songs_per_minute",
        "ad_intensity",
        "frustration_index",
        "is_heavy_user",
        "premium_no_offline",
        "engagement_score",
        "frustration_index_v2",
        "premium_value",
        "high_skip_low_time",
        "free_heavy_ads",
        "songs_per_hour"
    ],
    "categorical_features": [
        "gender",
        "country",
        "subscription_type",
        "device_type"
    ],
    "export_date": "2024-01-15 10:30:00"
}
```

**Importante**: Atualize também:
- `version`: "2.0"
- `model_type`: "XGBoost"
- `accuracy`, `recall`, `precision`, `f1_score`, `auc_roc`: com os valores do Colab
- `threshold_otimo`: com o threshold calculado
- `export_date`: data atual

---

### 3. Copiar Modelo ONNX

```bash
# Copiar modelo baixado do Colab
cp modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx
```

---

### 4. Recompilar Aplicação

```bash
# Recompilar
mvn clean package -DskipTests

# Ou com Docker
docker-compose build app
```

---

### 5. Reiniciar Aplicação

```bash
# Reiniciar container
docker-compose restart app

# Verificar logs
docker-compose logs -f app | grep -i "threshold\|metadata"
```

Você deve ver algo como:

```
INFO - Metadados do modelo carregados - Nome: Spotify Churn Model XGBoost v2.0 | Threshold: 0.4521
INFO - Modelo ONNX carregado com sucesso
```

---

### 6. Testar Nova Predição

```bash
curl -X POST http://localhost:10808/predict \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "test-123",
    "gender": "Male",
    "age": 25,
    "country": "BR",
    "subscription_type": "Free",
    "device_type": "Mobile",
    "listening_time": 300,
    "songs_played_per_day": 10,
    "skip_rate": 0.7,
    "ads_listened_per_week": 50,
    "offline_listening": false
  }'
```

**Resultado Esperado**:

```json
{
  "prediction": "Risco Alto de Cancelamento",
  "probability": 0.7234,
  "decision_threshold": 0.4521,  ← NOVO THRESHOLD
  "risk_level": "Alto Risco de Churn",
  "ai_diagnosis": {
    "primary_risk_factor": "Anúncios por Semana",
    "suggested_action": "Oferecer desconto Premium"
  }
}
```

---

## ✅ Checklist de Validação

Após atualizar, verifique:

- [ ] `metadata.json` atualizado com novo threshold
- [ ] `modelo_hackathon.onnx` substituído
- [ ] Aplicação recompilada
- [ ] Container reiniciado
- [ ] Logs mostram novo threshold
- [ ] Predição de teste funciona
- [ ] Threshold aparece na resposta da API

---

## 🔍 Como o Threshold é Usado

### No Código

O threshold é lido automaticamente do `metadata.json`:

```java
// ChurnPredictionService.java
private ChurnStatus determinarStatus(double probabilidadeChurn) {
    return probabilidadeChurn >= modelMetadataPort.getThresholdOtimo()
        ? ChurnStatus.WILL_CHURN
        : ChurnStatus.WILL_STAY;
}
```

### Exemplo de Decisão

```
Probabilidade: 0.45
Threshold:     0.4521

0.45 < 0.4521 → WILL_STAY (não vai cancelar)

Probabilidade: 0.75
Threshold:     0.4521

0.75 >= 0.4521 → WILL_CHURN (vai cancelar)
```

---

## 📊 Comparação Antes vs Depois

### Modelo Antigo (Logistic Regression)
```json
{
  "threshold_otimo": 0.262755,
  "auc_roc": 0.544,
  "accuracy": 0.6488
}
```

**Comportamento**: Classifica como CHURN se probabilidade >= 26.3%  
**Problema**: Threshold muito baixo, muitos falsos positivos

---

### Modelo Novo (XGBoost)
```json
{
  "threshold_otimo": 0.4521,
  "auc_roc": 0.7834,
  "accuracy": 0.7834
}
```

**Comportamento**: Classifica como CHURN se probabilidade >= 45.2%  
**Vantagem**: Threshold otimizado, melhor balanço precision/recall

---

## 🐛 Troubleshooting

### Erro: "Threshold não atualizado"

**Causa**: Arquivo não foi recarregado

**Solução**:
```bash
# Forçar rebuild
docker-compose down
docker-compose up --build
```

---

### Erro: "Modelo ONNX incompatível"

**Causa**: Número de features diferente

**Solução**: Verificar se `numeric_features` no metadata.json corresponde às features do modelo

---

### Predições Estranhas

**Causa**: Threshold muito alto ou muito baixo

**Solução**: Ajustar threshold manualmente:

```json
{
  "threshold_otimo": 0.5  // Testar valores entre 0.3 e 0.7
}
```

---

## 💡 Dicas

### Threshold Muito Baixo (< 0.3)
- **Efeito**: Muitos falsos positivos
- **Quando usar**: Quando custo de perder cliente é muito alto

### Threshold Balanceado (0.4 - 0.6)
- **Efeito**: Balanço entre precision e recall
- **Quando usar**: Uso geral (recomendado)

### Threshold Muito Alto (> 0.7)
- **Efeito**: Muitos falsos negativos
- **Quando usar**: Quando recursos de retenção são limitados

---

## 📞 Suporte

### Dúvidas?
- Consulte: `docs/IMPLEMENTAR_XGBOOST_AGORA.md`
- Logs: `docker-compose logs app`
- Teste: `curl http://localhost:10808/actuator/health`

---

**Equipe DataBeats** | ChurnInsight  
**Versão**: 1.0  
**Data**: Janeiro 2024
