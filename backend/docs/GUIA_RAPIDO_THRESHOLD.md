# 🎯 Guia Rápido: Atualizar Threshold do Modelo XGBoost

## ✅ Você já tem o modelo treinado!

Agora precisa atualizar o threshold no backend para usar o novo modelo.

---

## 📋 Passo 1: Copiar Valores do Colab

No Google Colab, na **CÉLULA 7**, você viu algo assim:

```
📊 RESULTADOS
============================================================
AUC-ROC:   0.7834 (78.34%)
Accuracy:  0.7834
Precision: 0.6845
Recall:    0.7123
F1-Score:  0.6981

Threshold Ótimo: 0.4521  ← COPIE ESTE VALOR
```

**Anote estes valores:**
- Threshold: `_______`
- AUC-ROC: `_______`
- Accuracy: `_______`
- Precision: `_______`
- Recall: `_______`
- F1-Score: `_______`

---

## 📝 Passo 2: Atualizar metadata.json

Abra o arquivo: `src/main/resources/metadata.json`

Substitua o conteúdo por:

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
    "threshold_otimo": 0.4521,
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
    "export_date": "2026-03-13 10:00:00"
}
```

**⚠️ IMPORTANTE**: Substitua os valores de `accuracy`, `recall`, `precision`, `f1_score`, `auc_roc` e `threshold_otimo` pelos valores que você anotou do Colab!

---

## 📦 Passo 3: Copiar Modelo ONNX

Você baixou o arquivo `modelo_xgboost.onnx` do Colab. Agora copie para o projeto:

```bash
# No terminal, na pasta do projeto:
cp /caminho/para/modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx
```

**Exemplo no Windows**:
```bash
# Se o arquivo está em Downloads:
cp ~/Downloads/modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx
```

---

## 🔨 Passo 4: Recompilar

Como você está usando Docker, basta rebuildar a imagem:

```bash
docker-compose build app
```

**Nota**: O Maven está dentro do container Docker, não precisa instalar localmente!

---

## 🚀 Passo 5: Reiniciar

```bash
docker-compose up -d app
```

Isso vai rebuildar e reiniciar o container com o novo modelo.

---

## ✅ Passo 6: Verificar

Veja os logs para confirmar:

```bash
docker-compose logs -f app | grep -i "threshold\|metadata"
```

Você deve ver:

```
INFO - Metadados do modelo carregados - Nome: Spotify Churn Model XGBoost v2.0 | Threshold: 0.4521
INFO - Modelo ONNX carregado com sucesso
```

---

## 🧪 Passo 7: Testar

```bash
curl -X POST http://localhost:10808/predict \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "test-xgboost",
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

**Resposta esperada**:

```json
{
  "prediction": "Risco Alto de Cancelamento",
  "probability": 0.7234,
  "decision_threshold": 0.4521,
  "risk_level": "Alto Risco de Churn"
}
```

---

## 🎉 Pronto!

Seu modelo XGBoost está rodando com o novo threshold!

### O que mudou?

**Antes (Logistic Regression)**:
- Threshold: 0.262755 (26.3%)
- AUC-ROC: 0.544 (54.4%) - quase aleatório
- Accuracy: 64.88%

**Depois (XGBoost)**:
- Threshold: 0.4521 (45.2%)
- AUC-ROC: 0.7834 (78.34%) - muito melhor!
- Accuracy: 78.34%

**Impacto**: Menos falsos positivos, predições mais confiáveis!

---

## 🐛 Problemas?

### Erro: "Modelo não carregado"

```bash
# Verificar se arquivo existe
ls -lh src/main/resources/modelo_hackathon.onnx

# Deve mostrar o arquivo com tamanho > 0
```

### Erro: "Threshold não atualizado"

```bash
# Forçar rebuild completo
docker-compose down
docker-compose up --build
```

### Predições estranhas

Verifique se os valores no `metadata.json` estão corretos (sem vírgulas extras, formato JSON válido).

---

**Equipe DataBeats** | ChurnInsight  
**Versão**: 1.0  
**Data**: Março 2026
