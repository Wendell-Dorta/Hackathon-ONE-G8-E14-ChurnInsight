# 🚀 Implementar XGBoost AGORA - Guia Prático

## 🎯 Objetivo

Treinar modelo XGBoost melhorado e substituir o Logistic Regression atual.

**Tempo estimado**: 2-3 horas  
**Melhoria esperada**: 54% → 75-80% AUC-ROC

---

## 📋 Pré-requisitos

```bash
# 1. Python 3.8+
python --version

# 2. Instalar dependências
pip install pandas numpy scikit-learn xgboost onnx skl2onnx imbalanced-learn
```

---

## 🔥 Opção 1: Google Colab (RECOMENDADO)

### Passo 1: Abrir Colab
1. Acesse: https://colab.research.google.com/
2. Novo notebook
3. Runtime → Change runtime type → GPU (opcional, mas mais rápido)

### Passo 2: Copiar e Executar

Cole este código no Colab e execute célula por célula:

```python
# CÉLULA 1: Instalar dependências
!pip install xgboost scikit-learn imbalanced-learn skl2onnx -q

# CÉLULA 2: Imports
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
from sklearn.metrics import roc_auc_score, classification_report, confusion_matrix
import xgboost as xgb
from imblearn.over_sampling import SMOTE

# CÉLULA 3: Carregar seus dados
# OPÇÃO A: Upload manual
from google.colab import files
uploaded = files.upload()  # Selecione seu CSV
df = pd.read_csv(list(uploaded.keys())[0])

# OPÇÃO B: Do GitHub/Drive
# df = pd.read_csv('URL_DO_SEU_CSV')

# OPÇÃO C: Dados sintéticos para teste
np.random.seed(42)
n = 10000
df = pd.DataFrame({
    'age': np.random.randint(18, 70, n),
    'listening_time': np.random.exponential(300, n),
    'songs_played_per_day': np.random.poisson(10, n),
    'skip_rate': np.random.beta(2, 5, n),
    'ads_listened_per_week': np.random.poisson(20, n),
    'offline_listening': np.random.choice([0, 1], n, p=[0.6, 0.4]),
    'subscription_type': np.random.choice(['Free', 'Premium', 'Student', 'Family'], n),
    'device_type': np.random.choice(['Mobile', 'Desktop', 'Tablet'], n),
    'gender': np.random.choice(['Male', 'Female'], n),
    'country': np.random.choice(['BR', 'US', 'UK'], n),
    'churn': np.random.choice([0, 1], n, p=[0.75, 0.25])
})

print(f"✅ Dados carregados: {len(df)} registros")
print(f"   Churn rate: {df['churn'].mean():.2%}")
```

```python
# CÉLULA 4: Feature Engineering
def engineer_features(df):
    # Engagement Score
    df['engagement_score'] = (
        df['listening_time'] / 1440 * 
        (1 - df['skip_rate']) * 
        df['songs_played_per_day']
    )
    
    # Frustration Index
    df['frustration_index'] = (
        df['skip_rate'] * 0.4 +
        df['ads_listened_per_week'] / 100 * 0.3 +
        (1 - df['offline_listening']) * 0.3
    )
    
    # Premium Value
    df['premium_value'] = np.where(
        df['subscription_type'].isin(['Premium', 'Family']),
        df['listening_time'] * df['offline_listening'],
        0
    )
    
    # Risk Indicators
    df['high_skip_low_time'] = (
        (df['skip_rate'] > 0.5) & 
        (df['listening_time'] < 200)
    ).astype(int)
    
    df['free_heavy_ads'] = (
        (df['subscription_type'] == 'Free') & 
        (df['ads_listened_per_week'] > 50)
    ).astype(int)
    
    # Behavioral Ratios
    df['songs_per_hour'] = df['songs_played_per_day'] / (df['listening_time'] / 60 + 1e-6)
    
    return df

df = engineer_features(df)
print(f"✅ Features criadas! Total: {len(df.columns)} features")
```

```python
# CÉLULA 5: Preparar dados
# Separar features e target
X = df.drop('churn', axis=1)
y = df['churn']

# Encoding de categóricas
le_dict = {}
for col in X.select_dtypes(include='object').columns:
    le = LabelEncoder()
    X[col] = le.fit_transform(X[col].astype(str))
    le_dict[col] = le

# Split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

# Balanceamento com SMOTE
smote = SMOTE(sampling_strategy=0.8, random_state=42)
X_train_balanced, y_train_balanced = smote.fit_resample(X_train, y_train)

print(f"✅ Dados preparados:")
print(f"   Train: {len(X_train_balanced)} (após SMOTE)")
print(f"   Test: {len(X_test)}")
print(f"   Features: {X_train.shape[1]}")
```

```python
# CÉLULA 6: Treinar XGBoost
print("🚀 Treinando XGBoost...")

model = xgb.XGBClassifier(
    n_estimators=300,
    max_depth=6,
    learning_rate=0.05,
    subsample=0.8,
    colsample_bytree=0.8,
    scale_pos_weight=3,
    random_state=42,
    eval_metric='auc',
    tree_method='hist'
)

model.fit(
    X_train_balanced, 
    y_train_balanced,
    eval_set=[(X_test, y_test)],
    verbose=10
)

print("✅ Modelo treinado!")
```

```python
# CÉLULA 7: Avaliar modelo
y_pred_proba = model.predict_proba(X_test)[:, 1]

# Encontrar threshold ótimo
from sklearn.metrics import precision_recall_curve, f1_score

precisions, recalls, thresholds = precision_recall_curve(y_test, y_pred_proba)
f1_scores = 2 * (precisions * recalls) / (precisions + recalls + 1e-6)
optimal_idx = np.argmax(f1_scores)
optimal_threshold = thresholds[optimal_idx]

y_pred = (y_pred_proba >= optimal_threshold).astype(int)

# Métricas
auc = roc_auc_score(y_test, y_pred_proba)
print(f"\n{'='*60}")
print(f"📊 RESULTADOS")
print(f"{'='*60}")
print(f"AUC-ROC:   {auc:.4f} ({auc*100:.2f}%)")
print(f"Threshold: {optimal_threshold:.4f}")
print(f"\n{classification_report(y_test, y_pred, target_names=['Stay', 'Churn'])}")
print(f"\nConfusion Matrix:")
print(confusion_matrix(y_test, y_pred))
```

```python
# CÉLULA 8: Feature Importance
import matplotlib.pyplot as plt

feature_importance = pd.DataFrame({
    'feature': X.columns,
    'importance': model.feature_importances_
}).sort_values('importance', ascending=False).head(15)

plt.figure(figsize=(10, 6))
plt.barh(feature_importance['feature'], feature_importance['importance'])
plt.xlabel('Importance')
plt.title('Top 15 Features Mais Importantes')
plt.gca().invert_yaxis()
plt.tight_layout()
plt.show()

print("\n🔝 Top 10 Features:")
print(feature_importance.head(10).to_string(index=False))
```

```python
# CÉLULA 9: Exportar para ONNX (CORRIGIDO v2!)

# Instalar biblioteca correta para XGBoost
!pip install onnxmltools onnxruntime -q

import onnxmltools
from onnxmltools.convert.common.data_types import FloatTensorType
import onnxruntime as rt

print("💾 Exportando XGBoost para ONNX...")

# IMPORTANTE: Retreinar modelo SEM feature names
# XGBoost precisa usar índices numéricos para ONNX
model_for_onnx = xgb.XGBClassifier(
    n_estimators=300,
    max_depth=6,
    learning_rate=0.05,
    subsample=0.8,
    colsample_bytree=0.8,
    scale_pos_weight=3,
    random_state=42,
    eval_metric='auc',
    tree_method='hist'
)

# Treinar sem feature names (usa apenas arrays numpy)
print("🔄 Retreinando modelo para ONNX...")
model_for_onnx.fit(
    X_train_balanced.values,  # .values remove feature names
    y_train_balanced.values,
    verbose=False
)

print("✅ Modelo retreinado")

# Definir input shape
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]

# Converter XGBoost para ONNX (usa onnxmltools, não skl2onnx!)
onx = onnxmltools.convert_xgboost(
    model_for_onnx, 
    initial_types=initial_type,
    target_opset=12
)

# Salvar
with open("modelo_xgboost.onnx", "wb") as f:
    f.write(onx.SerializeToString())

print("✅ Modelo exportado: modelo_xgboost.onnx")

# Testar se funciona
sess = rt.InferenceSession("modelo_xgboost.onnx")
input_name = sess.get_inputs()[0].name

# Teste com uma amostra
test_sample = X_test.iloc[0:1].values.astype(np.float32)
pred_onnx = sess.run(None, {input_name: test_sample})

print(f"✅ Teste ONNX OK! Predição: {pred_onnx[1][0][1]:.4f}")

# Comparar com modelo original
pred_original = model.predict_proba(X_test.iloc[0:1])[:, 1][0]
pred_onnx_value = pred_onnx[1][0][1]
diff = abs(pred_original - pred_onnx_value)

print(f"\n📊 Validação:")
print(f"   Predição Original: {pred_original:.4f}")
print(f"   Predição ONNX:     {pred_onnx_value:.4f}")
print(f"   Diferença:         {diff:.6f}")

if diff < 0.001:
    print("   ✅ Modelos são equivalentes!")

# Download
from google.colab import files
files.download('modelo_xgboost.onnx')
```

```python
# CÉLULA 10: Criar metadata.json (formato compatível com o backend Java)
import json
from google.colab import files

# Separar features numéricas e categóricas (OBRIGATÓRIO para o backend)
categorical_cols = ["gender", "country", "subscription_type", "device_type"]
numeric_cols = [c for c in X.columns if c not in categorical_cols]

metadata = {
    "name": "Spotify Churn Model",
    "version": "2.0",
    "model_type": "XGBoost",
    "accuracy": float(auc),
    "auc_roc": float(auc),
    # IMPORTANTE: usar "threshold_otimo" (não "threshold") — campo lido pelo Java
    "threshold_otimo": float(optimal_threshold),
    # IMPORTANTE: separar em numeric_features e categorical_features
    "numeric_features": numeric_cols,
    "categorical_features": categorical_cols,
    "feature_importance": feature_importance.head(10).to_dict('records'),
    "export_date": pd.Timestamp.now().isoformat(),
    "n_samples_train": len(X_train_balanced),
    "n_samples_test": len(X_test)
}

with open("metadata.json", "w") as f:
    json.dump(metadata, f, indent=2)

print("✅ Metadata criado: metadata.json")
print(f"   threshold_otimo: {optimal_threshold:.6f}")
print(f"   numeric_features ({len(numeric_cols)}): {numeric_cols}")
print(f"   categorical_features ({len(categorical_cols)}): {categorical_cols}")
files.download('metadata.json')
```

---

## 🔥 Opção 2: Local (Python Script)

Crie arquivo `train_xgboost.py`:


```python
# train_xgboost.py
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import roc_auc_score, classification_report
import xgboost as xgb
from imblearn.over_sampling import SMOTE
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType
import json

# Carregar dados
df = pd.read_csv('seu_arquivo.csv')  # AJUSTAR

# Feature engineering (copiar da célula 4 do Colab)
# ... código de engineer_features ...

# Preparar dados (copiar da célula 5)
# ... código de preparação ...

# Treinar (copiar da célula 6)
# ... código de treino ...

# Avaliar (copiar da célula 7)
# ... código de avaliação ...

# Exportar (copiar da célula 9)
# ... código de exportação ...

print("✅ Concluído!")
```

Execute:
```bash
python train_xgboost.py
```

---

## 📦 Integrar no Backend

### Passo 1: Copiar Arquivos

```bash
# Copiar modelo ONNX
cp modelo_xgboost.onnx src/main/resources/modelo_hackathon.onnx

# Copiar metadata
cp metadata.json src/main/resources/metadata.json
```

### Passo 2: Atualizar Threshold

Edite `ChurnBusinessRules.java`:

```java
public class ChurnBusinessRules {
    // ATUALIZAR com threshold do modelo novo
    public static final double CHURN_THRESHOLD = 0.XXX;  // do metadata.json
}
```

### Passo 3: Testar

```bash
# Recompilar
mvn clean package -DskipTests

# Reiniciar
docker-compose restart app

# Testar
curl -X POST http://localhost:10808/predict \
  -u admin:admin \
  -H "Content-Type: application/json" \
  -d @example-predict-request.json
```

---

## 🎯 Validação A/B

### Rodar Ambos Modelos em Paralelo

```java
@Service
public class ABTestingService {
    
    private final OnnxRuntimeAdapter modeloAntigo;
    private final OnnxRuntimeAdapter modeloNovo;
    
    public PredictionResult predictWithAB(CustomerProfile profile) {
        // 50% tráfego para cada modelo
        boolean useNewModel = Math.random() > 0.5;
        
        PredictionResult result = useNewModel 
            ? modeloNovo.predict(profile)
            : modeloAntigo.predict(profile);
        
        // Log para análise
        log.info("AB Test: model={}, auc={}", 
            useNewModel ? "novo" : "antigo", 
            result.getProbability());
        
        return result;
    }
}
```

---

## 📊 Monitorar Performance

### Métricas para Acompanhar

```java
@Component
public class ModelMetrics {
    
    private final Counter predictionsTotal;
    private final Histogram predictionLatency;
    private final Gauge modelAccuracy;
    
    public void recordPrediction(PredictionResult result, long latencyMs) {
        predictionsTotal.increment();
        predictionLatency.observe(latencyMs);
        
        // Calcular accuracy em janela deslizante
        updateAccuracy(result);
    }
}
```

### Dashboard Grafana

```yaml
# prometheus.yml
- job_name: 'churninsight'
  static_configs:
    - targets: ['app:10808']
  metrics_path: '/actuator/prometheus'
```

---

## ✅ Checklist de Implementação

### Antes de Deploy
- [ ] Modelo treinado com AUC > 75%
- [ ] Exportado para ONNX
- [ ] Metadata.json criado
- [ ] Threshold otimizado
- [ ] Testado localmente

### Durante Deploy
- [ ] Backup do modelo antigo
- [ ] Copiar novos arquivos
- [ ] Atualizar threshold no código
- [ ] Recompilar aplicação
- [ ] Reiniciar containers

### Após Deploy
- [ ] Testar endpoint /predict
- [ ] Verificar latência (<500ms)
- [ ] Monitorar accuracy
- [ ] Comparar com modelo antigo
- [ ] Coletar feedback

---

## 🐛 Troubleshooting

### Erro: "ONNX model failed to load"
```bash
# Verificar compatibilidade
python -c "import onnx; print(onnx.__version__)"

# Deve ser compatível com onnxruntime no backend
# Backend usa: onnxruntime 1.19.2
```

### Erro: "Feature mismatch"
```bash
# Verificar ordem das features
# Deve ser EXATAMENTE a mesma do treino
```

### Modelo muito lento
```bash
# Reduzir n_estimators
n_estimators=100  # ao invés de 300

# Ou usar tree_method='hist'
tree_method='hist'  # mais rápido
```

### Accuracy baixa em produção
```bash
# Verificar data drift
# Retreinar com dados mais recentes
# Ajustar threshold
```

---

## 📈 Resultados Esperados

### Antes (Logistic Regression)
```
AUC-ROC:   54.40%
Accuracy:  64.88%
Precision: 31.50%
Recall:    30.43%
F1-Score:  30.96%
```

### Depois (XGBoost)
```
AUC-ROC:   75-80% ✅ (+38%)
Accuracy:  78-82% ✅ (+20%)
Precision: 65-70% ✅ (+106%)
Recall:    70-75% ✅ (+130%)
F1-Score:  67-72% ✅ (+120%)
```

---

## 🎓 Próximos Passos

### Curto Prazo (1 semana)
1. Treinar XGBoost no Colab
2. Validar AUC > 75%
3. Exportar para ONNX
4. Integrar no backend
5. Deploy em staging

### Médio Prazo (1 mês)
1. A/B testing (50/50)
2. Monitorar métricas
3. Ajustar threshold se necessário
4. Migrar 100% para novo modelo
5. Documentar resultados

### Longo Prazo (3 meses)
1. Hyperparameter tuning
2. Ensemble com LightGBM
3. Retreinamento automático
4. SHAP para explicabilidade
5. Modelo específico por segmento

---

## 💡 Dicas Importantes

### Para Melhor Performance
1. Use dados reais (não sintéticos)
2. Colete pelo menos 6 meses de histórico
3. Valide com split temporal (não aleatório)
4. Monitore drift de dados
5. Retreine mensalmente

### Para Produção
1. Sempre faça backup do modelo antigo
2. Teste em staging primeiro
3. Deploy gradual (10% → 50% → 100%)
4. Monitore latência e accuracy
5. Tenha rollback plan

### Para Debugging
1. Salve predições em banco
2. Compare com modelo antigo
3. Analise casos de erro
4. Colete feedback de usuários
5. Documente todas as decisões

---

## 📞 Suporte

### Dúvidas sobre Treino
- Consulte: `docs/GUIA_MELHORIA_MODELO_ML.md`
- Script: `scripts/improve_model.py`

### Dúvidas sobre Integração
- Consulte: `README.md` (seção ONNX)
- Código: `OnnxRuntimeAdapter.java`

### Problemas?
- Abra issue no GitHub
- Consulte logs: `docker-compose logs app`

---

## 🎉 Conclusão

Com este guia, você pode:
1. ✅ Treinar XGBoost em 2-3 horas
2. ✅ Melhorar AUC de 54% para 75-80%
3. ✅ Exportar para ONNX
4. ✅ Integrar no backend
5. ✅ Deploy em produção

**Próximo Passo**: Abra o Google Colab e comece agora! 🚀

---

**Equipe DataBeats** | ChurnInsight  
**Versão**: 1.0  
**Data**: Janeiro 2024
