#!/usr/bin/env python3
"""
Script Simples para Treinar XGBoost
Uso: python scripts/train_xgboost_simple.py
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import roc_auc_score, classification_report, confusion_matrix, precision_recall_curve
import xgboost as xgb
from imblearn.over_sampling import SMOTE
import json
import warnings
warnings.filterwarnings('ignore')

print("="*60)
print("🚀 TREINAR MODELO XGBOOST - ChurnInsight")
print("="*60)

# ============================================================================
# 1. CARREGAR DADOS
# ============================================================================
print("\n📂 Carregando dados...")

# OPÇÃO A: Carregar do CSV (AJUSTAR O CAMINHO)
try:
    df = pd.read_csv('data/churn_data.csv')
    print(f"✅ Dados carregados do CSV: {len(df)} registros")
except FileNotFoundError:
    print("⚠️ CSV não encontrado. Usando dados sintéticos para demonstração...")
    
    # OPÇÃO B: Dados sintéticos para teste
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
    print(f"✅ Dados sintéticos criados: {len(df)} registros")

print(f"   Churn rate: {df['churn'].mean():.2%}")

# ============================================================================
# 2. FEATURE ENGINEERING
# ============================================================================
print("\n🔧 Criando features avançadas...")

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

print(f"✅ Features criadas! Total: {len(df.columns)} features")

# ============================================================================
# 3. PREPARAR DADOS
# ============================================================================
print("\n🔧 Preparando dados...")

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

# ============================================================================
# 4. TREINAR XGBOOST
# ============================================================================
print("\n🚀 Treinando XGBoost...")

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

# ============================================================================
# 5. AVALIAR MODELO
# ============================================================================
print("\n📊 Avaliando modelo...")

y_pred_proba = model.predict_proba(X_test)[:, 1]

# Encontrar threshold ótimo
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
cm = confusion_matrix(y_test, y_pred)
print(f"   TN: {cm[0,0]:5d}  |  FP: {cm[0,1]:5d}")
print(f"   FN: {cm[1,0]:5d}  |  TP: {cm[1,1]:5d}")

# ============================================================================
# 6. FEATURE IMPORTANCE
# ============================================================================
print("\n🔝 Top 10 Features Mais Importantes:")

feature_importance = pd.DataFrame({
    'feature': X.columns,
    'importance': model.feature_importances_
}).sort_values('importance', ascending=False)

print(feature_importance.head(10).to_string(index=False))

# ============================================================================
# 7. EXPORTAR PARA ONNX
# ============================================================================
print("\n💾 Exportando para ONNX...")

try:
    import onnxmltools
    from onnxmltools.convert.common.data_types import FloatTensorType
    import onnxruntime as rt
    
    # Definir input shape
    initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]
    
    # Converter XGBoost para ONNX (usa onnxmltools, não skl2onnx!)
    onx = onnxmltools.convert_xgboost(
        model, 
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
    test_sample = X_test.iloc[0:1].values.astype(np.float32)
    pred_onnx = sess.run(None, {input_name: test_sample})
    print(f"✅ Teste ONNX OK! Predição: {pred_onnx[1][0][1]:.4f}")
    
except ImportError:
    print("⚠️ onnxmltools não instalado. Instale com: pip install onnxmltools onnxruntime")
    print("   Modelo não foi exportado para ONNX")

# ============================================================================
# 8. CRIAR METADATA
# ============================================================================
print("\n📝 Criando metadata...")

metadata = {
    "model_type": "XGBoost",
    "version": "2.0",
    "auc_roc": float(auc),
    "threshold": float(optimal_threshold),
    "n_features": int(X_train.shape[1]),
    "features": X.columns.tolist(),
    "top_features": feature_importance.head(10)[['feature', 'importance']].to_dict('records'),
    "training_date": pd.Timestamp.now().isoformat(),
    "n_samples_train": int(len(X_train_balanced)),
    "n_samples_test": int(len(X_test)),
    "churn_rate_train": float(y_train.mean()),
    "churn_rate_test": float(y_test.mean())
}

with open("metadata.json", "w") as f:
    json.dump(metadata, f, indent=2)

print("✅ Metadata criado: metadata.json")

# ============================================================================
# 9. RESUMO FINAL
# ============================================================================
print(f"\n{'='*60}")
print(f"✅ TREINAMENTO CONCLUÍDO!")
print(f"{'='*60}")
print(f"\n📊 Métricas Finais:")
print(f"   AUC-ROC: {auc:.4f} ({auc*100:.2f}%)")
print(f"   Threshold: {optimal_threshold:.4f}")
print(f"\n📁 Arquivos Gerados:")
print(f"   ✅ modelo_xgboost.onnx")
print(f"   ✅ metadata.json")
print(f"\n🚀 Próximos Passos:")
print(f"   1. Copiar modelo_xgboost.onnx para src/main/resources/")
print(f"   2. Atualizar threshold em ChurnBusinessRules.java")
print(f"   3. Recompilar e testar")
print(f"\n{'='*60}")
