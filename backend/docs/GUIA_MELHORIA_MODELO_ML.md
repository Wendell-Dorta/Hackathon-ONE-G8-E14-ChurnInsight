# 🎯 Guia Completo: Melhorando a Precisão do Modelo de Churn

## 📊 Situação Atual

**Métricas do Modelo Atual**:
```
Accuracy:   64.88%  ⚠️ Baixo
Precision:  31.50%  ⚠️ Muito Baixo (muitos falsos positivos)
Recall:     30.43%  ⚠️ Muito Baixo (perde muitos churners)
F1-Score:   30.96%  ⚠️ Muito Baixo
AUC-ROC:    54.40%  ⚠️ Quase aleatório (50% = coin flip)
```

**Diagnóstico**: O modelo atual está **pouco melhor que aleatório** e tem sérios problemas de generalização.

---

## 🎯 Meta de Melhoria

### Objetivos Realistas

| Métrica | Atual | Meta Curto Prazo | Meta Longo Prazo |
|---------|-------|------------------|------------------|
| Accuracy | 64.88% | 75-80% | 85%+ |
| Precision | 31.50% | 60-70% | 75%+ |
| Recall | 30.43% | 65-75% | 80%+ |
| F1-Score | 30.96% | 62-72% | 77%+ |
| AUC-ROC | 54.40% | 75-80% | 85%+ |

**Benchmark da Indústria**: Modelos de churn bem-sucedidos atingem AUC-ROC de 80-90%.

---

## 🔍 Análise de Problemas

### 1. Modelo Muito Simples
**Problema**: Logistic Regression é linear e limitado  
**Impacto**: Não captura relações complexas

### 2. Features Insuficientes
**Problema**: Apenas 15 features, algumas pouco informativas  
**Impacto**: Modelo não tem informação suficiente

### 3. Desbalanceamento de Classes
**Problema**: SMOTE pode estar gerando dados sintéticos ruins  
**Impacto**: Modelo aprende padrões artificiais

### 4. Threshold Não Otimizado
**Problema**: Threshold de 0.263 pode não ser ideal  
**Impacto**: Trade-off precision/recall não otimizado

### 5. Sem Validação Temporal
**Problema**: Treino/teste podem ter data leakage  
**Impacto**: Overfitting temporal

---

## 🚀 Estratégias de Melhoria

## FASE 1: Quick Wins (1-2 semanas)

### 1.1 Feature Engineering Avançado 🔧

**Criar Features Temporais**:
```python
# Engagement Score
df['engagement_score'] = (
    df['listening_time'] / 1440  # normalizar por dia
    * (1 - df['skip_rate'])
    * df['songs_played_per_day']
)

# Frustration Index (já existe, mas melhorar)
df['frustration_index_v2'] = (
    df['skip_rate'] * 0.4 +
    df['ads_listened_per_week'] / 100 * 0.3 +
    (1 - df['offline_listening']) * 0.3
)

# Premium Value Score
df['premium_value'] = np.where(
    df['subscription_type'].isin(['Premium', 'Family']),
    df['listening_time'] * df['offline_listening'],
    0
)

# Churn Risk Indicators
df['high_skip_low_time'] = (
    (df['skip_rate'] > 0.5) & 
    (df['listening_time'] < 200)
).astype(int)

df['free_heavy_ads'] = (
    (df['subscription_type'] == 'Free') & 
    (df['ads_listened_per_week'] > 50)
).astype(int)

# Interaction Features
df['age_subscription_interaction'] = (
    df['age'] * 
    df['subscription_type'].map({
        'Free': 0, 'Student': 1, 'Premium': 2, 
        'Duo': 2, 'Family': 3
    })
)

# Behavioral Ratios
df['songs_per_hour'] = df['songs_played_per_day'] / (df['listening_time'] / 60)
df['skip_per_song'] = df['skip_rate'] * df['songs_played_per_day']
```

**Impacto Esperado**: +5-10% accuracy

---

### 1.2 Otimizar Threshold 📊

**Encontrar Threshold Ótimo**:
```python
from sklearn.metrics import precision_recall_curve, f1_score

# Calcular curva precision-recall
precisions, recalls, thresholds = precision_recall_curve(y_true, y_proba)

# Encontrar threshold que maximiza F1
f1_scores = 2 * (precisions * recalls) / (precisions + recalls)
optimal_idx = np.argmax(f1_scores)
optimal_threshold = thresholds[optimal_idx]

print(f"Threshold ótimo: {optimal_threshold:.3f}")
print(f"F1-Score: {f1_scores[optimal_idx]:.3f}")

# Ou maximizar F-beta (dar mais peso ao recall)
from sklearn.metrics import fbeta_score
beta = 2  # recall 2x mais importante que precision
fbeta_scores = ((1 + beta**2) * precisions * recalls) / 
               (beta**2 * precisions + recalls)
optimal_idx = np.argmax(fbeta_scores)
```

**Impacto Esperado**: +3-5% F1-score

---

### 1.3 Balanceamento de Classes Melhorado ⚖️

**Testar Diferentes Estratégias**:
```python
# 1. Class Weights (mais simples)
from sklearn.linear_model import LogisticRegression
model = LogisticRegression(class_weight='balanced')

# 2. SMOTE com diferentes ratios
from imblearn.over_sampling import SMOTE, ADASYN, BorderlineSMOTE
smote = SMOTE(sampling_strategy=0.8)  # 80% da classe majoritária
X_resampled, y_resampled = smote.fit_resample(X_train, y_train)

# 3. Undersampling + Oversampling
from imblearn.combine import SMOTETomek
smt = SMOTETomek(random_state=42)
X_resampled, y_resampled = smt.fit_resample(X_train, y_train)

# 4. ADASYN (adaptativo)
adasyn = ADASYN(sampling_strategy=0.8)
X_resampled, y_resampled = adasyn.fit_resample(X_train, y_train)
```

**Impacto Esperado**: +5-8% recall

---

## FASE 2: Modelos Mais Complexos (2-3 semanas)

### 2.1 Testar Modelos Avançados 🤖

**Ordem de Teste**:

#### 1. Random Forest (Baseline Forte)
```python
from sklearn.ensemble import RandomForestClassifier

rf = RandomForestClassifier(
    n_estimators=200,
    max_depth=15,
    min_samples_split=10,
    min_samples_leaf=5,
    class_weight='balanced',
    random_state=42,
    n_jobs=-1
)

rf.fit(X_train, y_train)
```

**Vantagens**: Robusto, captura não-linearidades, feature importance  
**Esperado**: AUC-ROC 70-75%

---

#### 2. XGBoost (Melhor Performance)
```python
import xgboost as xgb

xgb_model = xgb.XGBClassifier(
    n_estimators=300,
    max_depth=6,
    learning_rate=0.05,
    subsample=0.8,
    colsample_bytree=0.8,
    scale_pos_weight=3,  # para desbalanceamento
    random_state=42,
    eval_metric='auc'
)

xgb_model.fit(
    X_train, y_train,
    eval_set=[(X_val, y_val)],
    early_stopping_rounds=20,
    verbose=10
)
```

**Vantagens**: Estado da arte, regularização, early stopping  
**Esperado**: AUC-ROC 75-82%

---

#### 3. LightGBM (Mais Rápido)
```python
import lightgbm as lgb

lgb_model = lgb.LGBMClassifier(
    n_estimators=300,
    max_depth=8,
    learning_rate=0.05,
    num_leaves=31,
    subsample=0.8,
    colsample_bytree=0.8,
    class_weight='balanced',
    random_state=42
)

lgb_model.fit(
    X_train, y_train,
    eval_set=[(X_val, y_val)],
    eval_metric='auc',
    callbacks=[lgb.early_stopping(20)]
)
```

**Vantagens**: Muito rápido, eficiente em memória  
**Esperado**: AUC-ROC 74-80%

---

#### 4. CatBoost (Melhor para Categóricas)
```python
from catboost import CatBoostClassifier

cat_model = CatBoostClassifier(
    iterations=500,
    depth=6,
    learning_rate=0.05,
    l2_leaf_reg=3,
    auto_class_weights='Balanced',
    random_seed=42,
    verbose=50
)

cat_model.fit(
    X_train, y_train,
    cat_features=['gender', 'country', 'subscription_type', 'device_type'],
    eval_set=(X_val, y_val),
    early_stopping_rounds=20
)
```

**Vantagens**: Lida bem com categóricas, robusto  
**Esperado**: AUC-ROC 75-81%

---

#### 5. Neural Network (Deep Learning)
```python
import tensorflow as tf
from tensorflow import keras

model = keras.Sequential([
    keras.layers.Dense(128, activation='relu', input_shape=(n_features,)),
    keras.layers.Dropout(0.3),
    keras.layers.BatchNormalization(),
    keras.layers.Dense(64, activation='relu'),
    keras.layers.Dropout(0.2),
    keras.layers.Dense(32, activation='relu'),
    keras.layers.Dense(1, activation='sigmoid')
])

model.compile(
    optimizer=keras.optimizers.Adam(0.001),
    loss='binary_crossentropy',
    metrics=['AUC', 'Precision', 'Recall']
)

# Class weights para desbalanceamento
class_weight = {0: 1, 1: 3}

model.fit(
    X_train, y_train,
    validation_data=(X_val, y_val),
    epochs=100,
    batch_size=256,
    class_weight=class_weight,
    callbacks=[
        keras.callbacks.EarlyStopping(patience=10, restore_best_weights=True),
        keras.callbacks.ReduceLROnPlateau(patience=5)
    ]
)
```

**Vantagens**: Captura padrões complexos  
**Esperado**: AUC-ROC 72-78%

---

### 2.2 Ensemble de Modelos 🎯

**Combinar Melhores Modelos**:
```python
from sklearn.ensemble import VotingClassifier

# Soft voting (média de probabilidades)
ensemble = VotingClassifier(
    estimators=[
        ('xgb', xgb_model),
        ('lgb', lgb_model),
        ('cat', cat_model)
    ],
    voting='soft',
    weights=[2, 1, 1]  # XGBoost tem peso maior
)

ensemble.fit(X_train, y_train)
```

**Stacking (Meta-Learner)**:
```python
from sklearn.ensemble import StackingClassifier

stacking = StackingClassifier(
    estimators=[
        ('xgb', xgb_model),
        ('lgb', lgb_model),
        ('cat', cat_model),
        ('rf', rf_model)
    ],
    final_estimator=LogisticRegression(),
    cv=5
)

stacking.fit(X_train, y_train)
```

**Impacto Esperado**: +2-5% AUC-ROC sobre melhor modelo individual

---

## FASE 3: Otimização Avançada (2-3 semanas)

### 3.1 Hyperparameter Tuning 🎛️

**Optuna (Recomendado)**:
```python
import optuna

def objective(trial):
    params = {
        'n_estimators': trial.suggest_int('n_estimators', 100, 500),
        'max_depth': trial.suggest_int('max_depth', 3, 10),
        'learning_rate': trial.suggest_float('learning_rate', 0.01, 0.3, log=True),
        'subsample': trial.suggest_float('subsample', 0.6, 1.0),
        'colsample_bytree': trial.suggest_float('colsample_bytree', 0.6, 1.0),
        'scale_pos_weight': trial.suggest_float('scale_pos_weight', 1, 5)
    }
    
    model = xgb.XGBClassifier(**params, random_state=42)
    model.fit(X_train, y_train)
    
    y_pred_proba = model.predict_proba(X_val)[:, 1]
    auc = roc_auc_score(y_val, y_pred_proba)
    
    return auc

study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=100, timeout=3600)

print(f"Melhor AUC: {study.best_value:.4f}")
print(f"Melhores params: {study.best_params}")
```

**Impacto Esperado**: +3-7% AUC-ROC

---

### 3.2 Feature Selection 🎯

**Remover Features Irrelevantes**:
```python
from sklearn.feature_selection import SelectFromModel

# 1. Feature Importance (XGBoost)
selector = SelectFromModel(xgb_model, threshold='median')
X_train_selected = selector.fit_transform(X_train, y_train)

# 2. Recursive Feature Elimination
from sklearn.feature_selection import RFECV
rfecv = RFECV(estimator=xgb_model, cv=5, scoring='roc_auc')
rfecv.fit(X_train, y_train)
print(f"Features ótimas: {rfecv.n_features_}")

# 3. Permutation Importance
from sklearn.inspection import permutation_importance
perm_importance = permutation_importance(
    xgb_model, X_val, y_val, 
    n_repeats=10, random_state=42
)

# Remover features com importância < threshold
important_features = perm_importance.importances_mean > 0.01
X_train_filtered = X_train[:, important_features]
```

**Impacto Esperado**: +2-4% AUC-ROC (reduz overfitting)

---

### 3.3 Validação Temporal ⏰

**Evitar Data Leakage**:
```python
from sklearn.model_selection import TimeSeriesSplit

# Split temporal (não aleatório!)
tscv = TimeSeriesSplit(n_splits=5)

scores = []
for train_idx, val_idx in tscv.split(X):
    X_train, X_val = X[train_idx], X[val_idx]
    y_train, y_val = y[train_idx], y[val_idx]
    
    model.fit(X_train, y_train)
    score = roc_auc_score(y_val, model.predict_proba(X_val)[:, 1])
    scores.append(score)

print(f"AUC médio: {np.mean(scores):.4f} ± {np.std(scores):.4f}")
```

**Impacto**: Métrica mais realista (pode ser menor, mas mais confiável)

---

## FASE 4: Dados e Contexto (Contínuo)

### 4.1 Coletar Mais Dados 📊

**Prioridades**:
1. **Dados Temporais**: Histórico de comportamento (últimos 3-6 meses)
2. **Dados de Interação**: Playlists criadas, artistas favoritos, compartilhamentos
3. **Dados de Suporte**: Tickets abertos, reclamações, NPS
4. **Dados de Pagamento**: Atrasos, métodos de pagamento, histórico de upgrades

**Impacto Esperado**: +10-15% AUC-ROC (maior impacto!)

---

### 4.2 Features Externas 🌐

**Enriquecer com Dados Externos**:
```python
# 1. Dados Demográficos
# - Renda média por região
# - Penetração de internet
# - Concorrência local

# 2. Dados Sazonais
df['month'] = df['created_at'].dt.month
df['is_holiday_season'] = df['month'].isin([11, 12]).astype(int)
df['is_summer'] = df['month'].isin([6, 7, 8]).astype(int)

# 3. Dados de Mercado
# - Preço de concorrentes
# - Lançamentos de álbuns populares
# - Eventos musicais
```

**Impacto Esperado**: +3-5% AUC-ROC

---

## 📊 Plano de Implementação

### Semana 1-2: Quick Wins
```
✅ Feature engineering avançado
✅ Otimizar threshold
✅ Testar diferentes estratégias de balanceamento
✅ Baseline com Random Forest
```
**Meta**: AUC-ROC 70-72%

---

### Semana 3-4: Modelos Avançados
```
✅ Treinar XGBoost
✅ Treinar LightGBM
✅ Treinar CatBoost
✅ Comparar performance
```
**Meta**: AUC-ROC 75-78%

---

### Semana 5-6: Otimização
```
✅ Hyperparameter tuning (Optuna)
✅ Feature selection
✅ Ensemble de modelos
✅ Validação temporal
```
**Meta**: AUC-ROC 78-82%

---

### Semana 7+: Produção
```
✅ Exportar melhor modelo para ONNX
✅ Integrar no backend
✅ A/B testing (modelo novo vs antigo)
✅ Monitorar performance
```
**Meta**: AUC-ROC 80%+ em produção

---

## 🔄 Pipeline de Retreinamento

### Automatizar Melhoria Contínua

```python
# pipeline_retrain.py
import mlflow

def retrain_pipeline():
    # 1. Carregar dados novos
    df = load_new_data(last_n_days=30)
    
    # 2. Feature engineering
    df = engineer_features(df)
    
    # 3. Split temporal
    X_train, X_val, y_train, y_val = temporal_split(df)
    
    # 4. Treinar modelo
    model = train_best_model(X_train, y_train)
    
    # 5. Validar
    auc = evaluate_model(model, X_val, y_val)
    
    # 6. Log no MLflow
    with mlflow.start_run():
        mlflow.log_metric("auc", auc)
        mlflow.sklearn.log_model(model, "model")
    
    # 7. Deploy se melhor que atual
    if auc > current_model_auc:
        deploy_model(model)
        notify_team(f"Novo modelo deployed! AUC: {auc:.4f}")
    
    return model

# Agendar para rodar semanalmente
schedule.every().monday.at("02:00").do(retrain_pipeline)
```

---

## 📈 Métricas de Sucesso

### Curto Prazo (1-2 meses)
- [ ] AUC-ROC > 75%
- [ ] Precision > 60%
- [ ] Recall > 65%
- [ ] F1-Score > 62%

### Médio Prazo (3-6 meses)
- [ ] AUC-ROC > 80%
- [ ] Precision > 70%
- [ ] Recall > 75%
- [ ] F1-Score > 72%

### Longo Prazo (6-12 meses)
- [ ] AUC-ROC > 85%
- [ ] Precision > 75%
- [ ] Recall > 80%
- [ ] F1-Score > 77%

---

## 💰 Investimento Estimado

| Fase | Esforço | Custo* | Melhoria Esperada |
|------|---------|--------|-------------------|
| Fase 1: Quick Wins | 2 semanas | R$ 10K | +8-12% AUC |
| Fase 2: Modelos Avançados | 3 semanas | R$ 15K | +10-15% AUC |
| Fase 3: Otimização | 3 semanas | R$ 15K | +5-8% AUC |
| Fase 4: Dados + Contexto | Contínuo | R$ 20K | +10-15% AUC |
| **Total** | **8 semanas** | **R$ 60K** | **+33-50% AUC** |

*Baseado em R$ 5K/semana por cientista de dados

---

## 🎯 Recomendação Final

### Prioridade Máxima
1. ✅ **Feature Engineering** (maior impacto, baixo custo)
2. ✅ **XGBoost/LightGBM** (estado da arte)
3. ✅ **Hyperparameter Tuning** (otimização)

### Médio Prazo
4. Coletar mais dados (histórico temporal)
5. Ensemble de modelos
6. Retreinamento automático

### Longo Prazo
7. Deep Learning (se dados suficientes)
8. Features externas
9. Modelos específicos por segmento

---

## 📚 Recursos

### Cursos
- [Kaggle: Feature Engineering](https://www.kaggle.com/learn/feature-engineering)
- [Fast.ai: Practical Deep Learning](https://course.fast.ai/)
- [XGBoost Documentation](https://xgboost.readthedocs.io/)

### Papers
- [XGBoost: A Scalable Tree Boosting System](https://arxiv.org/abs/1603.02754)
- [LightGBM: A Highly Efficient Gradient Boosting Decision Tree](https://papers.nips.cc/paper/6907-lightgbm-a-highly-efficient-gradient-boosting-decision-tree)

### Ferramentas
- [Optuna](https://optuna.org/) - Hyperparameter tuning
- [MLflow](https://mlflow.org/) - Experiment tracking
- [SHAP](https://shap.readthedocs.io/) - Explicabilidade

---

**Conclusão**: Com as estratégias acima, é **totalmente viável** melhorar o modelo de 64.88% para **80%+ AUC-ROC** em 2-3 meses.

**Próximo Passo**: Começar pela Fase 1 (Quick Wins) para validar abordagem e gerar momentum!

---

**Equipe DataBeats** | ChurnInsight ML Excellence  
**Versão**: 1.0  
**Data**: Janeiro 2024
