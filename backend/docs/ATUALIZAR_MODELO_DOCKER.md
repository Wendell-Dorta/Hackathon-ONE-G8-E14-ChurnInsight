# 🐳 Atualizar Modelo XGBoost com Docker

## Passo a Passo Simplificado

### 1️⃣ Atualizar metadata.json

Abra: `src/main/resources/metadata.json`

Cole os valores do seu Colab:

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

**⚠️ Substitua** os valores pelos do seu Colab!

---

### 2️⃣ Copiar modelo ONNX

Copie o arquivo que você baixou do Colab:

```bash
cp modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx
```

**No Windows PowerShell**:
```powershell
Copy-Item modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx
```

**Ou arraste e solte** o arquivo `modelo_xgboost.onnx` para a pasta `src/main/resources/` e renomeie para `modelo_hackathon.onnx`

---

### 3️⃣ Rebuildar e Reiniciar

```bash
docker-compose build app
docker-compose up -d app
```

Ou tudo de uma vez:

```bash
docker-compose up -d --build app
```

---

### 4️⃣ Verificar Logs

```bash
docker-compose logs -f app
```

Procure por:

```
INFO - Metadados do modelo carregados - Nome: Spotify Churn Model XGBoost v2.0 | Threshold: 0.4521
INFO - Modelo ONNX carregado com sucesso
```

Pressione `Ctrl+C` para sair dos logs.

---

### 5️⃣ Testar

```bash
curl -X POST http://localhost:10808/predict -u admin:admin -H "Content-Type: application/json" -d "{\"user_id\":\"test-xgboost\",\"gender\":\"Male\",\"age\":25,\"country\":\"BR\",\"subscription_type\":\"Free\",\"device_type\":\"Mobile\",\"listening_time\":300,\"songs_played_per_day\":10,\"skip_rate\":0.7,\"ads_listened_per_week\":50,\"offline_listening\":false}"
```

**Ou use o Postman/Insomnia** com:
- URL: `http://localhost:10808/predict`
- Method: `POST`
- Auth: Basic (admin/admin)
- Body (JSON):

```json
{
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
}
```

---

## ✅ Pronto!

Seu modelo XGBoost está rodando!

### Checklist:

- [ ] `metadata.json` atualizado com valores do Colab
- [ ] `modelo_hackathon.onnx` substituído
- [ ] Docker rebuild executado
- [ ] Logs mostram novo threshold
- [ ] Teste de predição funcionou

---

## 🐛 Problemas Comuns

### Erro: "Arquivo não encontrado"

Verifique se o arquivo está no lugar certo:

```bash
ls -lh src/main/resources/modelo_hackathon.onnx
```

### Erro: "Container não inicia"

Veja os logs completos:

```bash
docker-compose logs app
```

### Erro: "Predição falha"

Verifique se o `metadata.json` está com JSON válido (sem vírgulas extras no final).

---

**Equipe DataBeats** | ChurnInsight  
**Data**: Março 2026
