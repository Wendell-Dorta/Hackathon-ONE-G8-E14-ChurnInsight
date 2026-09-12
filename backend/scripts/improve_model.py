"""
Script para Melhorar o Modelo de Churn
Implementa as estratégias do GUIA_MELHORIA_MODELO_ML.md

Uso:
    python scripts/improve_model.py --phase 1  # Quick Wins
    python scripts/improve_model.py --phase 2  # Modelos Avançados
    python scripts/improve_model.py --phase 3  # Otimização
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, TimeSeriesSplit
from sklearn.metrics import (
    roc_auc_score, precision_score, recall_score, 
    f1_score, classification_report, confusion_matrix
)
from sklearn.preprocessing import StandardScaler, LabelEncoder
import warnings
warnings.filterwarnings('ignore')

# ============================================================================
# FASE 1: FEATURE ENGINEERING
# ============================================================================

def engineer_features(df):
    """
    Cria features avançadas para melhorar o modelo
    """
    print("🔧 Criando features avançadas...")
    
    # 1. Engagement Score
    df['engagement_score'] = (
        df['listening_time'] / 1440  # normalizar por dia
        * (1 - df['skip_rate'])
        * df['songs_played_per_day']
    )
    
    # 2. Frustration Index v2
    df['frustration_index_v2'] = (
        df['skip_rate'] * 0.4 +
        df['ads_listened_per_week'] / 100 * 0.3 +
        (1 - df['offline_listening'].astype(int)) * 0.3
    )
    
    # 3. Premium Value Score
    df['premium_value'] = np.where(
        df['subscription_type'].isin(['Premium', 'Family']),
        df['listening_time'] * df['offline_listening'].astype(int),
        0
    )
    
    # 4. Churn Risk Indicators
    df['high_skip_low_time'] = (
        (df['skip_rate'] > 0.5) & 
        (df['listening_time'] < 200)
    ).astype(int)
    
    df['free_heavy_ads'] = (
        (df['subscription_type'] == 'Free') & 
        (df['ads_listened_per_week'] > 50)
    ).astype(int)
    
    # 5. Behavioral Ratios
    df['songs_per_hour'] = df['songs_played_per_day'] / (df['listening_time'] / 60 + 1e-6)
    df['skip_per_song'] = df['skip_rate'] * df['songs_played_per_day']
    
    # 6. Subscription Type Encoding (ordinal)
    subscription_order = {'Free': 0, 'Student': 1, 'Premium': 2, 'Duo': 2, 'Family': 3}
    df['subscription_level'] = df['subscription_type'].map(subscription_order)
    
    # 7. Age-Subscription Interaction
    df['age_subscription_interaction'] = df['age'] * df['subscription_level']
    
    print(f"✅ Features criadas! Total: {len(df.columns)} features")
    return df


def optimize_threshold(y_true, y_proba, metric='f1'):
    """
    Encontra o threshold ótimo para maximizar métrica escolhida
    """
    from sklearn.metrics import precision_recall_curve
    
    print(f"🎯 Otimizando threshold para maximizar {metric}...")
    
    precisions, recalls, thresholds = precision_recall_curve(y_true, y_proba)
    
    if metric == 'f1':
        # Maximizar F1-Score
        f1_scores = 2 * (precisions * recalls) / (precisions + recalls + 1e-6)
        optimal_idx = np.argmax(f1_scores)
        optimal_threshold = thresholds[optimal_idx]
        optimal_score = f1_scores[optimal_idx]
    
    elif metric == 'f2':
        # Maximizar F2-Score (recall 2x mais importante)
        beta = 2
        f2_scores = ((1 + beta**2) * precisions * recalls) / (beta**2 * precisions + recalls + 1e-6)
        optimal_idx = np.argmax(f2_scores)
        optimal_threshold = thresholds[optimal_idx]
        optimal_score = f2_scores[optimal_idx]
    
    print(f"✅ Threshold ótimo: {optimal_threshold:.4f}")
    print(f"   {metric.upper()}-Score: {optimal_score:.4f}")
    print(f"   Precision: {precisions[optimal_idx]:.4f}")
    print(f"   Recall: {recalls[optimal_idx]:.4f}")
    
    return optimal_threshold


# ============================================================================
# FASE 2: MODELOS AVANÇADOS
# ============================================================================

def train_xgboost(X_train, y_train, X_val, y_val):
    """
    Treina modelo XGBoost (melhor performance)
    """
    import xgboost as xgb
    
    print("🚀 Treinando XGBoost...")
    
    model = xgb.XGBClassifier(
        n_estimators=300,
        max_depth=6,
        learning_rate=0.05,
        subsample=0.8,
        colsample_bytree=0.8,
        scale_pos_weight=3,  # para desbalanceamento
        random_state=42,
        eval_metric='auc',
        tree_method='hist'  # mais rápido
    )
    
    model.fit(
        X_train, y_train,
        eval_set=[(X_val, y_val)],
        verbose=10
    )
    
    return model


def train_lightgbm(X_train, y_train, X_val, y_val):
    """
    Treina modelo LightGBM (mais rápido)
    """
    import lightgbm as lgb
    
    print("⚡ Treinando LightGBM...")
    
    model = lgb.LGBMClassifier(
        n_estimators=300,
        max_depth=8,
        learning_rate=0.05,
        num_leaves=31,
        subsample=0.8,
        colsample_bytree=0.8,
        class_weight='balanced',
        random_state=42,
        verbose=-1
    )
    
    model.fit(
        X_train, y_train,
        eval_set=[(X_val, y_val)],
        eval_metric='auc',
        callbacks=[lgb.early_stopping(20, verbose=False)]
    )
    
    return model


def train_random_forest(X_train, y_train):
    """
    Treina Random Forest (baseline forte)
    """
    from sklearn.ensemble import RandomForestClassifier
    
    print("🌲 Treinando Random Forest...")
    
    model = RandomForestClassifier(
        n_estimators=200,
        max_depth=15,
        min_samples_split=10,
        min_samples_leaf=5,
        class_weight='balanced',
        random_state=42,
        n_jobs=-1
    )
    
    model.fit(X_train, y_train)
    
    return model


# ============================================================================
# FASE 3: OTIMIZAÇÃO
# ============================================================================

def hyperparameter_tuning(X_train, y_train, X_val, y_val, n_trials=50):
    """
    Otimização de hiperparâmetros com Optuna
    """
    import optuna
    import xgboost as xgb
    
    print(f"🎛️ Otimizando hiperparâmetros ({n_trials} trials)...")
    
    def objective(trial):
        params = {
            'n_estimators': trial.suggest_int('n_estimators', 100, 500),
            'max_depth': trial.suggest_int('max_depth', 3, 10),
            'learning_rate': trial.suggest_float('learning_rate', 0.01, 0.3, log=True),
            'subsample': trial.suggest_float('subsample', 0.6, 1.0),
            'colsample_bytree': trial.suggest_float('colsample_bytree', 0.6, 1.0),
            'scale_pos_weight': trial.suggest_float('scale_pos_weight', 1, 5),
            'random_state': 42
        }
        
        model = xgb.XGBClassifier(**params)
        model.fit(X_train, y_train, verbose=False)
        
        y_pred_proba = model.predict_proba(X_val)[:, 1]
        auc = roc_auc_score(y_val, y_pred_proba)
        
        return auc
    
    study = optuna.create_study(direction='maximize')
    study.optimize(objective, n_trials=n_trials, show_progress_bar=True)
    
    print(f"✅ Melhor AUC: {study.best_value:.4f}")
    print(f"   Melhores params: {study.best_params}")
    
    return study.best_params


def create_ensemble(models, X_train, y_train):
    """
    Cria ensemble de modelos (voting)
    """
    from sklearn.ensemble import VotingClassifier
    
    print("🎯 Criando ensemble de modelos...")
    
    ensemble = VotingClassifier(
        estimators=models,
        voting='soft',
        weights=[2, 1, 1]  # XGBoost tem peso maior
    )
    
    ensemble.fit(X_train, y_train)
    
    return ensemble


# ============================================================================
# AVALIAÇÃO
# ============================================================================

def evaluate_model(model, X_test, y_test, threshold=0.5):
    """
    Avalia modelo com métricas completas
    """
    print("\n" + "="*60)
    print("📊 AVALIAÇÃO DO MODELO")
    print("="*60)
    
    # Predições
    y_pred_proba = model.predict_proba(X_test)[:, 1]
    y_pred = (y_pred_proba >= threshold).astype(int)
    
    # Métricas
    auc = roc_auc_score(y_test, y_pred_proba)
    precision = precision_score(y_test, y_pred)
    recall = recall_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    
    print(f"\n🎯 Métricas (threshold={threshold:.3f}):")
    print(f"   AUC-ROC:   {auc:.4f} ({auc*100:.2f}%)")
    print(f"   Precision: {precision:.4f} ({precision*100:.2f}%)")
    print(f"   Recall:    {recall:.4f} ({recall*100:.2f}%)")
    print(f"   F1-Score:  {f1:.4f} ({f1*100:.2f}%)")
    
    # Confusion Matrix
    cm = confusion_matrix(y_test, y_pred)
    print(f"\n📋 Confusion Matrix:")
    print(f"   TN: {cm[0,0]:5d}  |  FP: {cm[0,1]:5d}")
    print(f"   FN: {cm[1,0]:5d}  |  TP: {cm[1,1]:5d}")
    
    # Classification Report
    print(f"\n📈 Classification Report:")
    print(classification_report(y_test, y_pred, target_names=['Stay', 'Churn']))
    
    return {
        'auc': auc,
        'precision': precision,
        'recall': recall,
        'f1': f1,
        'threshold': threshold
    }


def compare_models(models_dict, X_test, y_test):
    """
    Compara múltiplos modelos
    """
    print("\n" + "="*60)
    print("🏆 COMPARAÇÃO DE MODELOS")
    print("="*60)
    
    results = []
    
    for name, model in models_dict.items():
        y_pred_proba = model.predict_proba(X_test)[:, 1]
        auc = roc_auc_score(y_test, y_pred_proba)
        
        # Encontrar threshold ótimo
        threshold = optimize_threshold(y_test, y_pred_proba, metric='f1')
        y_pred = (y_pred_proba >= threshold).astype(int)
        
        precision = precision_score(y_test, y_pred)
        recall = recall_score(y_test, y_pred)
        f1 = f1_score(y_test, y_pred)
        
        results.append({
            'Model': name,
            'AUC': f"{auc:.4f}",
            'Precision': f"{precision:.4f}",
            'Recall': f"{recall:.4f}",
            'F1': f"{f1:.4f}"
        })
    
    df_results = pd.DataFrame(results)
    print("\n" + df_results.to_string(index=False))
    
    return df_results


# ============================================================================
# EXPORTAR PARA ONNX
# ============================================================================

def export_to_onnx(model, X_sample, output_path='modelo_melhorado.onnx'):
    """
    Exporta modelo para ONNX
    """
    from skl2onnx import convert_sklearn
    from skl2onnx.common.data_types import FloatTensorType
    
    print(f"\n💾 Exportando modelo para ONNX...")
    
    initial_type = [('float_input', FloatTensorType([None, X_sample.shape[1]]))]
    onx = convert_sklearn(model, initial_types=initial_type)
    
    with open(output_path, "wb") as f:
        f.write(onx.SerializeToString())
    
    print(f"✅ Modelo exportado: {output_path}")


# ============================================================================
# MAIN
# ============================================================================

def main(phase=1):
    """
    Executa pipeline de melhoria do modelo
    """
    print("="*60)
    print("🚀 PIPELINE DE MELHORIA DO MODELO DE CHURN")
    print("="*60)
    
    # Carregar dados (exemplo - ajustar para seus dados)
    print("\n📂 Carregando dados...")
    # df = pd.read_csv('data/churn_data.csv')
    # Por enquanto, criar dados sintéticos para exemplo
    np.random.seed(42)
    n_samples = 10000
    
    df = pd.DataFrame({
        'age': np.random.randint(18, 70, n_samples),
        'listening_time': np.random.exponential(300, n_samples),
        'songs_played_per_day': np.random.poisson(10, n_samples),
        'skip_rate': np.random.beta(2, 5, n_samples),
        'ads_listened_per_week': np.random.poisson(20, n_samples),
        'offline_listening': np.random.choice([0, 1], n_samples, p=[0.6, 0.4]),
        'subscription_type': np.random.choice(['Free', 'Premium', 'Student', 'Family'], n_samples),
        'churn': np.random.choice([0, 1], n_samples, p=[0.75, 0.25])
    })
    
    print(f"✅ Dados carregados: {len(df)} registros")
    
    # FASE 1: Feature Engineering
    if phase >= 1:
        df = engineer_features(df)
    
    # Preparar dados
    print("\n🔧 Preparando dados...")
    
    # Separar features e target
    feature_cols = [col for col in df.columns if col != 'churn']
    X = df[feature_cols]
    y = df['churn']
    
    # Encoding de categóricas
    le = LabelEncoder()
    for col in X.select_dtypes(include='object').columns:
        X[col] = le.fit_transform(X[col].astype(str))
    
    # Split
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )
    X_train, X_val, y_train, y_val = train_test_split(
        X_train, y_train, test_size=0.2, random_state=42, stratify=y_train
    )
    
    # Normalização
    scaler = StandardScaler()
    X_train = scaler.fit_transform(X_train)
    X_val = scaler.transform(X_val)
    X_test = scaler.transform(X_test)
    
    print(f"✅ Train: {len(X_train)}, Val: {len(X_val)}, Test: {len(X_test)}")
    
    # FASE 2: Treinar Modelos
    if phase >= 2:
        print("\n" + "="*60)
        print("FASE 2: TREINANDO MODELOS AVANÇADOS")
        print("="*60)
        
        models = {}
        
        # Random Forest
        models['Random Forest'] = train_random_forest(X_train, y_train)
        
        # XGBoost
        try:
            models['XGBoost'] = train_xgboost(X_train, y_train, X_val, y_val)
        except ImportError:
            print("⚠️ XGBoost não instalado. Instale com: pip install xgboost")
        
        # LightGBM
        try:
            models['LightGBM'] = train_lightgbm(X_train, y_train, X_val, y_val)
        except ImportError:
            print("⚠️ LightGBM não instalado. Instale com: pip install lightgbm")
        
        # Comparar modelos
        compare_models(models, X_test, y_test)
        
        # Selecionar melhor modelo
        best_model = models['XGBoost'] if 'XGBoost' in models else models['Random Forest']
    
    else:
        # Apenas Random Forest para Fase 1
        best_model = train_random_forest(X_train, y_train)
    
    # FASE 3: Otimização
    if phase >= 3:
        print("\n" + "="*60)
        print("FASE 3: OTIMIZAÇÃO")
        print("="*60)
        
        # Hyperparameter tuning
        try:
            best_params = hyperparameter_tuning(X_train, y_train, X_val, y_val, n_trials=20)
            
            # Retreinar com melhores params
            import xgboost as xgb
            best_model = xgb.XGBClassifier(**best_params)
            best_model.fit(X_train, y_train)
        except ImportError:
            print("⚠️ Optuna não instalado. Instale com: pip install optuna")
    
    # Avaliação Final
    y_pred_proba = best_model.predict_proba(X_test)[:, 1]
    optimal_threshold = optimize_threshold(y_test, y_pred_proba, metric='f1')
    
    results = evaluate_model(best_model, X_test, y_test, threshold=optimal_threshold)
    
    # Exportar para ONNX
    try:
        export_to_onnx(best_model, X_test, 'modelo_melhorado.onnx')
    except ImportError:
        print("⚠️ skl2onnx não instalado. Instale com: pip install skl2onnx")
    
    print("\n" + "="*60)
    print("✅ PIPELINE CONCLUÍDO!")
    print("="*60)
    
    return best_model, results


if __name__ == "__main__":
    import argparse
    
    parser = argparse.ArgumentParser(description='Melhorar modelo de churn')
    parser.add_argument('--phase', type=int, default=1, choices=[1, 2, 3],
                        help='Fase do pipeline (1=Quick Wins, 2=Modelos Avançados, 3=Otimização)')
    
    args = parser.parse_args()
    
    model, results = main(phase=args.phase)
    
    print(f"\n🎉 Melhoria concluída! AUC-ROC: {results['auc']:.4f}")
