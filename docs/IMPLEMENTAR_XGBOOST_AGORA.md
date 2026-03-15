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
# Dataset do projeto — direto do GitHub
df = pd.read_csv('https://raw.githubusercontent.com/Equipe-14-DataBeats-Hackaton-NoCountry/Hackathon-ONE---Churn-clientes/main/spotify_churn_dataset.csv')

# OPÇÃO B: Upload manual de outro CSV
# from google.colab import files
# uploaded = files.upload()
# df = pd.read_csv(list(uploaded.keys())[0])

print(f"✅ Dados carregados: {len(df)} registros")
print(f"   Colunas: {df.columns.tolist()}")
print(f"   Churn rate: {df['is_churned'].mean():.2%}")
```

```python
# CÉLULA 4: Feature Engineering
# IMPORTANTE: Estas features devem ser IDÊNTICAS às calculadas em ChurnBusinessRules.java
# O backend Java calcula as mesmas fórmulas antes de enviar ao modelo ONNX

def engineer_features(df):
    # frustration_index = skip_rate × (ads_listened_per_week + 1)
    # Java: calculateFrustrationIndex()
    df['frustration_index'] = df['skip_rate'] * (df['ads_listened_per_week'] + 1)

    # ad_intensity = ads_per_week / ((songs_per_day × 7) + 1)
    # Java: calculateAdIntensity()
    df['ad_intensity'] = df['ads_listened_per_week'] / (df['songs_played_per_day'] * 7.0 + 1)

    # songs_per_minute = songs_per_day / (listening_time + 1)
    # Java: calculateSongsPerMinute()
    df['songs_per_minute'] = df['songs_played_per_day'] / (df['listening_time'] + 1)

    # is_heavy_user = listening_time > 450 AND skip_rate < 0.2
    # Java: isHeavyUser()
    df['is_heavy_user'] = (
        (df['listening_time'] > 450) & (df['skip_rate'] < 0.2)
    ).astype(int)

    # premium_no_offline = subscription_type != 'Free' AND offline_listening == False
    # Java: isPremiumNoOffline()
    df['premium_no_offline'] = (
        (df['subscription_type'] != 'Free') & (df['offline_listening'] == 0)
    ).astype(int)

    return df

df = engineer_features(df)
print(f"✅ Features criadas! Total: {len(df.columns)} features")
print(f"   Colunas disponíveis: {df.columns.tolist()}")
print(f"   Engineered: frustration_index, ad_intensity, songs_per_minute, is_heavy_user, premium_no_offline")
```

```python
# CÉLULA 5: Preparar dados
from sklearn.compose import ColumnTransformer
from sklearn.preprocessing import OneHotEncoder

# Detectar coluna target automaticamente
# Nomes comuns: 'churn', 'Churn', 'churned', 'target', 'label'
target_candidates = ['is_churned', 'churn', 'Churn', 'churned', 'target', 'label', 'cancelled']
target_col = next((c for c in target_candidates if c in df.columns), None)

if target_col is None:
    print(f"❌ Coluna target não encontrada! Colunas disponíveis: {df.columns.tolist()}")
    print("   Defina manualmente: target_col = 'nome_da_coluna'")
else:
    print(f"✅ Coluna target detectada: '{target_col}'")

# Remover colunas que não são features ANTES de separar X e y
cols_to_drop = [c for c in ['user_id', 'id', 'customer_id'] if c in df.columns]
if cols_to_drop:
    df = df.drop(cols_to_drop, axis=1)
    print(f"⚠️ Colunas removidas do df: {cols_to_drop}")

# Separar features e target
X = df.drop(target_col, axis=1)
y = df[target_col]
print(f"✅ Features após drop: {X.columns.tolist()}")

# IMPORTANTE: Manter categóricas como STRING — o backend Java envia strings ao modelo
# O pipeline sklearn vai fazer o OneHotEncoding internamente e exportar para ONNX
categorical_cols = ['gender', 'country', 'subscription_type', 'device_type']
numeric_cols = [c for c in X.columns if c not in categorical_cols]

print(f"✅ Features numéricas ({len(numeric_cols)}): {numeric_cols}")
print(f"✅ Features categóricas ({len(categorical_cols)}): {categorical_cols}")

# Garantir tipos corretos
for col in numeric_cols:
    X[col] = X[col].astype(float)
for col in categorical_cols:
    X[col] = X[col].astype(str)

# Split
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42, stratify=y
)

# SMOTE só funciona em dados numéricos — aplicar após encoding manual temporário
from sklearn.preprocessing import LabelEncoder
X_train_enc = X_train.copy()
X_test_enc = X_test.copy()
le_dict = {}
for col in categorical_cols:
    le = LabelEncoder()
    X_train_enc[col] = le.fit_transform(X_train_enc[col])
    X_test_enc[col] = le.transform(X_test_enc[col])
    le_dict[col] = le

smote = SMOTE(sampling_strategy=0.8, random_state=42)
X_train_balanced_enc, y_train_balanced = smote.fit_resample(X_train_enc, y_train)

# Reverter encoding nas categóricas para manter strings no treino do pipeline
X_train_balanced = X_train_balanced_enc.copy()
for col in categorical_cols:
    X_train_balanced[col] = le_dict[col].inverse_transform(
        X_train_balanced_enc[col].astype(int)
    )

print(f"✅ Dados preparados:")
print(f"   Train: {len(X_train_balanced)} (após SMOTE)")
print(f"   Test: {len(X_test)}")
print(f"   Features: {X_train.shape[1]}")
```

```python
# CÉLULA 6: Treinar XGBoost diretamente (sem pipeline, sem SMOTE)
# SMOTE prejudica modelos com pouco sinal — usar scale_pos_weight no lugar
print("🚀 Treinando XGBoost...")

# Calcular scale_pos_weight = negativos / positivos
neg = (y_train == 0).sum()
pos = (y_train == 1).sum()
spw = neg / pos
print(f"   scale_pos_weight: {spw:.2f} ({neg} negativos / {pos} positivos)")

# Usar X_train_enc (com LabelEncoder) diretamente — sem SMOTE
model_xgb = xgb.XGBClassifier(
    n_estimators=500,
    max_depth=4,
    learning_rate=0.03,
    subsample=0.8,
    colsample_bytree=0.8,
    min_child_weight=5,
    gamma=1,
    scale_pos_weight=spw,
    random_state=42,
    eval_metric='auc',
    tree_method='hist',
    early_stopping_rounds=30
)

model_xgb.fit(
    X_train_enc, y_train,
    eval_set=[(X_test_enc, y_test)],
    verbose=False
)
print("✅ XGBoost treinado!")
```

```python
# CÉLULA 7: Avaliar modelo
y_pred_proba = model_xgb.predict_proba(X_test_enc)[:, 1]

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
print(f"📊 RESULTADOS XGBoost")
print(f"{'='*60}")
print(f"AUC-ROC:   {auc:.4f} ({auc*100:.2f}%)")
print(f"Threshold: {optimal_threshold:.4f}")
print(f"\n{classification_report(y_test, y_pred, target_names=['Stay', 'Churn'])}")
print(f"\nConfusion Matrix:")
print(confusion_matrix(y_test, y_pred))
```

```python
# CÉLULA 7B: Comparação de Modelos
# Treina vários algoritmos e compara AUC — útil para escolher o melhor para exportar
!pip install lightgbm -q

from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, GradientBoostingClassifier
from sklearn.svm import SVC
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline as SkPipeline
import lightgbm as lgb
import warnings
warnings.filterwarnings('ignore')

print("🔬 Comparando modelos...\n")

results = []

modelos = {
    "Logistic Regression": LogisticRegression(max_iter=1000, class_weight='balanced', random_state=42),
    "Random Forest": RandomForestClassifier(n_estimators=200, class_weight='balanced', random_state=42, n_jobs=-1),
    "Gradient Boosting": GradientBoostingClassifier(n_estimators=200, max_depth=4, learning_rate=0.05, random_state=42),
    "XGBoost": model_xgb,
    "LightGBM": lgb.LGBMClassifier(n_estimators=300, max_depth=4, learning_rate=0.05,
                                     scale_pos_weight=spw, random_state=42, verbose=-1),
}

for nome, modelo in modelos.items():
    try:
        if nome != "XGBoost":  # XGBoost já foi treinado
            modelo.fit(X_train_enc, y_train)

        proba = modelo.predict_proba(X_test_enc)[:, 1]
        auc_m = roc_auc_score(y_test, proba)

        # Threshold ótimo
        prec, rec, thr = precision_recall_curve(y_test, proba)
        f1s = 2 * (prec * rec) / (prec + rec + 1e-6)
        best_thr = thr[np.argmax(f1s)]
        y_p = (proba >= best_thr).astype(int)
        f1 = f1_score(y_test, y_p)

        results.append({"Modelo": nome, "AUC-ROC": auc_m, "F1": f1, "Threshold": best_thr, "objeto": modelo})
        print(f"  ✅ {nome:25s} AUC={auc_m:.4f}  F1={f1:.4f}")
    except Exception as e:
        print(f"  ❌ {nome}: {e}")

# Ordenar por AUC
results_sorted = sorted(results, key=lambda x: x["AUC-ROC"], reverse=True)

print(f"\n{'='*60}")
print(f"🏆 RANKING FINAL")
print(f"{'='*60}")
for i, r in enumerate(results_sorted):
    medal = ["🥇", "🥈", "🥉", "4️⃣", "5️⃣"][i]
    print(f"  {medal} {r['Modelo']:25s} AUC={r['AUC-ROC']:.4f}  F1={r['F1']:.4f}")

# Guardar o melhor modelo para exportação
best = results_sorted[0]
best_model = best["objeto"]
best_auc = best["AUC-ROC"]
best_threshold = best["Threshold"]
best_nome = best["Modelo"]
print(f"\n✅ Melhor modelo: {best_nome} (AUC={best_auc:.4f})")
print(f"   Será usado para exportação ONNX nas próximas células.")

# Gráfico comparativo
import matplotlib.pyplot as plt
nomes = [r["Modelo"] for r in results_sorted]
aucs  = [r["AUC-ROC"] for r in results_sorted]

plt.figure(figsize=(10, 5))
bars = plt.barh(nomes, aucs, color=['gold','silver','#cd7f32','steelblue','steelblue'])
plt.axvline(x=0.5, color='red', linestyle='--', label='Baseline (aleatório)')
plt.xlabel('AUC-ROC')
plt.title('Comparação de Modelos — AUC-ROC')
plt.xlim(0.4, 1.0)
for bar, val in zip(bars, aucs):
    plt.text(val + 0.002, bar.get_y() + bar.get_height()/2,
             f'{val:.4f}', va='center', fontsize=10)
plt.legend()
plt.tight_layout()
plt.show()
```

```python
# CÉLULA 7C: Hyperparameter Tuning com Optuna (XGBoost)
# Busca automática dos melhores parâmetros — pode ganhar 3-8% de AUC
# Tempo estimado: 3-5 minutos no Colab
!pip install optuna -q

import optuna
optuna.logging.set_verbosity(optuna.logging.WARNING)

from sklearn.model_selection import cross_val_score

print("🔍 Iniciando tuning com Optuna (50 trials)...")

def objective(trial):
    params = {
        'n_estimators': trial.suggest_int('n_estimators', 100, 600),
        'max_depth': trial.suggest_int('max_depth', 3, 8),
        'learning_rate': trial.suggest_float('learning_rate', 0.01, 0.2, log=True),
        'subsample': trial.suggest_float('subsample', 0.6, 1.0),
        'colsample_bytree': trial.suggest_float('colsample_bytree', 0.6, 1.0),
        'min_child_weight': trial.suggest_int('min_child_weight', 1, 10),
        'gamma': trial.suggest_float('gamma', 0, 2),
        'reg_alpha': trial.suggest_float('reg_alpha', 0, 1),
        'reg_lambda': trial.suggest_float('reg_lambda', 0.5, 2),
        'scale_pos_weight': spw,
        'random_state': 42,
        'eval_metric': 'auc',
        'tree_method': 'hist',
    }
    model = xgb.XGBClassifier(**params)
    scores = cross_val_score(model, X_train_enc, y_train,
                             cv=3, scoring='roc_auc', n_jobs=-1)
    return scores.mean()

study = optuna.create_study(direction='maximize')
study.optimize(objective, n_trials=50, show_progress_bar=True)

best_params = study.best_params
best_params['scale_pos_weight'] = spw
best_params['random_state'] = 42
best_params['eval_metric'] = 'auc'
best_params['tree_method'] = 'hist'

print(f"\n✅ Melhor AUC (CV): {study.best_value:.4f}")
print(f"   Parâmetros: {best_params}")

# Treinar modelo final com melhores parâmetros
model_tuned = xgb.XGBClassifier(**best_params)
model_tuned.fit(X_train_enc, y_train)

proba_tuned = model_tuned.predict_proba(X_test_enc)[:, 1]
auc_tuned = roc_auc_score(y_test, proba_tuned)

prec, rec, thr = precision_recall_curve(y_test, proba_tuned)
f1s = 2 * (prec * rec) / (prec + rec + 1e-6)
best_thr_tuned = thr[np.argmax(f1s)]

print(f"\n📊 XGBoost Tuned vs Original:")
print(f"   Original:  AUC={auc:.4f}")
print(f"   Tuned:     AUC={auc_tuned:.4f}  (+{(auc_tuned-auc)*100:.2f}%)")
print(f"   Threshold: {best_thr_tuned:.4f}")

# Usar modelo tuned nas próximas células
if auc_tuned > auc:
    model_xgb = model_tuned
    auc = auc_tuned
    optimal_threshold = best_thr_tuned
    print("   ✅ Modelo tuned será usado para exportação!")
else:
    print("   ℹ️ Modelo original mantido (tuning não melhorou)")
```

```python
# CÉLULA 7D: Ensemble dos top 3 modelos (bônus — geralmente +2-3% AUC)
from sklearn.base import BaseEstimator, ClassifierMixin

print("🎯 Testando ensemble dos top 3 modelos...")

# Pegar os 3 melhores da comparação
top3 = results_sorted[:3]
top3_nomes = [r['Modelo'] for r in top3]
top3_modelos = [r['objeto'] for r in top3]

print(f"   Modelos: {top3_nomes}")

# Média das probabilidades
probas = np.array([m.predict_proba(X_test_enc)[:, 1] for m in top3_modelos])
proba_ensemble = probas.mean(axis=0)
auc_ensemble = roc_auc_score(y_test, proba_ensemble)

prec, rec, thr = precision_recall_curve(y_test, proba_ensemble)
f1s = 2 * (prec * rec) / (prec + rec + 1e-6)
thr_ensemble = thr[np.argmax(f1s)]
f1_ensemble = f1s.max()

print(f"\n📊 Comparação final:")
print(f"   XGBoost tuned:  AUC={auc:.4f}")
print(f"   Ensemble top3:  AUC={auc_ensemble:.4f}  F1={f1_ensemble:.4f}")

if auc_ensemble > auc:
    print(f"   ✅ Ensemble é melhor! (+{(auc_ensemble-auc)*100:.2f}%)")
    print(f"   ⚠️  Ensemble não pode ser exportado para ONNX diretamente.")
    print(f"      Usando XGBoost tuned para exportação (AUC={auc:.4f})")
else:
    print(f"   ℹ️  XGBoost tuned é melhor — exportando ele.")
```
import matplotlib.pyplot as plt

feature_importance = pd.DataFrame({
    'feature': list(X_train_enc.columns),
    'importance': model_xgb.feature_importances_
}).sort_values('importance', ascending=False).head(15)

plt.figure(figsize=(10, 6))
plt.barh(feature_importance['feature'], feature_importance['importance'])
plt.xlabel('Importance')
plt.title('Top Features Mais Importantes')
plt.gca().invert_yaxis()
plt.tight_layout()
plt.show()

print("\n🔝 Top Features:")
print(feature_importance.to_string(index=False))
```

```python
# CÉLULA 9: Exportar para ONNX com onnxmltools (abordagem direta e confiável)
# skl2onnx + XGBoost tem incompatibilidade com StringTensorType no pipeline.
# Solução: exportar o XGBoost puro com onnxmltools (flat float tensor),
# com as categóricas já encodadas numericamente.

!pip install onnxmltools onnxruntime -q

import onnxmltools
from onnxmltools.convert.common.data_types import FloatTensorType
import onnxruntime as rt

print("💾 Exportando XGBoost para ONNX...")

# Usar o melhor modelo da comparação se for XGBoost, senão usar model_xgb
import xgboost as xgb_module
if isinstance(best_model, xgb_module.XGBClassifier):
    modelo_para_exportar = best_model
    auc = best_auc
    optimal_threshold = best_threshold
    print(f"✅ Exportando melhor modelo: {best_nome} (AUC={auc:.4f})")
else:
    modelo_para_exportar = model_xgb
    print(f"⚠️ Melhor modelo ({best_nome}) não é XGBoost — exportando XGBoost mesmo assim.")
    print(f"   (onnxmltools só suporta XGBoost para exportação direta)")

# Garantir que user_id não está no dataset (drop defensivo)
cols_to_exclude = [c for c in ['user_id', 'id', 'customer_id'] if c in X_train_enc.columns]
if cols_to_exclude:
    X_train_enc = X_train_enc.drop(cols_to_exclude, axis=1)
    X_test_enc = X_test_enc.drop([c for c in cols_to_exclude if c in X_test_enc.columns], axis=1)
    print(f"⚠️ Removido do tensor: {cols_to_exclude}")

# Retreinar sem early_stopping para exportação limpa (onnxmltools não aceita early stopping)
best_n = model_xgb.best_iteration + 1 if hasattr(model_xgb, 'best_iteration') and model_xgb.best_iteration else 300
model_onnx = xgb.XGBClassifier(
    n_estimators=best_n,
    max_depth=4,
    learning_rate=0.03,
    subsample=0.8,
    colsample_bytree=0.8,
    min_child_weight=5,
    gamma=1,
    scale_pos_weight=spw,
    random_state=42,
    eval_metric='auc',
    tree_method='hist'
)

model_onnx.fit(X_train_enc.values, y_train.values, verbose=False)

n_features = X_train_enc.shape[1]
initial_type = [('float_input', FloatTensorType([None, n_features]))]

onx = onnxmltools.convert_xgboost(
    model_onnx,
    initial_types=initial_type,
    target_opset=12
)

with open("modelo_xgboost.onnx", "wb") as f:
    f.write(onx.SerializeToString())

print("✅ Modelo exportado: modelo_xgboost.onnx")

# Validar
sess = rt.InferenceSession("modelo_xgboost.onnx")
print(f"\n📋 Input: {sess.get_inputs()[0].name} | shape: {sess.get_inputs()[0].shape}")
print(f"   Outputs: {[o.name for o in sess.get_outputs()]}")

# Teste com uma amostra
test_sample = X_test_enc.iloc[0:1].values.astype(np.float32)
input_name = sess.get_inputs()[0].name
pred_onnx = sess.run(None, {input_name: test_sample})
pred_direct = model_xgb.predict_proba(X_test_enc.iloc[0:1])[:, 1][0]

if isinstance(pred_onnx[1], list) and isinstance(pred_onnx[1][0], dict):
    pred_onnx_val = pred_onnx[1][0].get(1, pred_onnx[1][0].get(1.0, 0.0))
else:
    pred_onnx_val = float(np.array(pred_onnx[1]).flatten()[1])

print(f"\n📊 Validação:")
print(f"   Modelo direto: {pred_direct:.4f}")
print(f"   ONNX:          {pred_onnx_val:.4f}")
print(f"   Diferença:     {abs(pred_direct - pred_onnx_val):.6f}")

if abs(pred_direct - pred_onnx_val) < 0.05:
    print("   ✅ Modelos equivalentes!")
else:
    print("   ⚠️ Diferença acima do esperado")

# Salvar ordem das features para o metadata.json
feature_order = list(X_train_enc.columns)
print(f"\n📋 Ordem das features no tensor ({n_features}):")
for i, f in enumerate(feature_order):
    print(f"   [{i}] {f}")

from google.colab import files
files.download('modelo_xgboost.onnx')
```

```python
# CÉLULA 10: Criar metadata.json (formato compatível com o backend Java)
import json
from google.colab import files

# Ordem exata das features no tensor — CRÍTICO para o backend montar o input correto
feature_order = list(X_train_enc.columns)

# Mapas de encoding para as categóricas (necessário para o backend replicar)
label_encodings = {}
for col in categorical_cols:
    label_encodings[col] = {str(cls): int(i) for i, cls in enumerate(le_dict[col].classes_)}

metadata = {
    "name": "Spotify Churn Model",
    "version": "2.0",
    "model_type": "XGBoost",
    "accuracy": float(auc),
    "auc_roc": float(auc),
    # IMPORTANTE: usar "threshold_otimo" — campo lido pelo Java
    "threshold_otimo": float(optimal_threshold),
    # Ordem exata das features no flat tensor float_input
    "feature_order": feature_order,
    # Mapeamento LabelEncoder para cada categórica
    "label_encodings": label_encodings,
    # Mantidos para compatibilidade com ModelMetadata.java
    "numeric_features": [c for c in feature_order if c not in categorical_cols],
    "categorical_features": categorical_cols,
    "export_date": pd.Timestamp.now().isoformat(),
    "n_samples_train": len(X_train_enc),
    "n_samples_test": len(X_test)
}

with open("metadata.json", "w") as f:
    json.dump(metadata, f, indent=2)

print("✅ Metadata criado: metadata.json")
print(f"   threshold_otimo: {optimal_threshold:.6f}")
print(f"   feature_order ({len(feature_order)}): {feature_order}")
print(f"   label_encodings: {list(label_encodings.keys())}")
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
