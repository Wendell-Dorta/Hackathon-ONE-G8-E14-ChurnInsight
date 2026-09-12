# 🤖 Scripts de Machine Learning - ChurnInsight

## 📁 Conteúdo

### `improve_model.py`
Script principal para melhorar o modelo de churn seguindo as estratégias do guia.

### `requirements_ml.txt`
Dependências Python necessárias para treinar modelos avançados.

---

## 🚀 Quick Start

### 1. Instalar Dependências

```bash
pip install -r scripts/requirements_ml.txt
```

### 2. Executar Pipeline

#### Fase 1: Quick Wins (Feature Engineering)
```bash
python scripts/improve_model.py --phase 1
```

**Resultado Esperado**: AUC-ROC 70-72%

---

#### Fase 2: Modelos Avançados (XGBoost, LightGBM)
```bash
python scripts/improve_model.py --phase 2
```

**Resultado Esperado**: AUC-ROC 75-78%

---

#### Fase 3: Otimização (Hyperparameter Tuning)
```bash
python scripts/improve_model.py --phase 3
```

**Resultado Esperado**: AUC-ROC 78-82%

---

## 📊 O que o Script Faz

### Fase 1: Quick Wins
1. ✅ Feature Engineering avançado
   - Engagement score
   - Frustration index v2
   - Premium value score
   - Behavioral ratios
   - Interaction features

2. ✅ Otimização de threshold
   - Encontra threshold que maximiza F1-Score
   - Balanceia precision e recall

3. ✅ Baseline com Random Forest
   - Modelo robusto e interpretável
   - Feature importance

---

### Fase 2: Modelos Avançados
1. ✅ Random Forest (baseline)
2. ✅ XGBoost (melhor performance)
3. ✅ LightGBM (mais rápido)
4. ✅ Comparação de modelos
5. ✅ Seleção do melhor

---

### Fase 3: Otimização
1. ✅ Hyperparameter tuning com Optuna
2. ✅ Feature selection
3. ✅ Ensemble de modelos
4. ✅ Validação cruzada

---

## 📈 Métricas Reportadas

O script reporta:
- **AUC-ROC**: Área sob a curva ROC
- **Precision**: Proporção de predições positivas corretas
- **Recall**: Proporção de positivos reais identificados
- **F1-Score**: Média harmônica de precision e recall
- **Confusion Matrix**: TN, FP, FN, TP
- **Classification Report**: Métricas por classe

---

## 💾 Saída

### Modelo ONNX
O script exporta o melhor modelo para:
```
modelo_melhorado.onnx
```

Este arquivo pode ser usado diretamente no backend Java com ONNX Runtime.

---

## 🔧 Personalização

### Usar Seus Próprios Dados

Edite a função `main()` em `improve_model.py`:

```python
# Substituir dados sintéticos por seus dados reais
df = pd.read_csv('data/churn_data.csv')

# Ou carregar do banco de dados
import mysql.connector
conn = mysql.connector.connect(...)
df = pd.read_sql("SELECT * FROM churn_history", conn)
```

---

### Ajustar Hiperparâmetros

Edite as funções de treinamento:

```python
def train_xgboost(X_train, y_train, X_val, y_val):
    model = xgb.XGBClassifier(
        n_estimators=500,      # aumentar
        max_depth=8,           # aumentar
        learning_rate=0.03,    # diminuir
        # ...
    )
```

---

### Adicionar Novos Modelos

```python
def train_catboost(X_train, y_train, X_val, y_val):
    from catboost import CatBoostClassifier
    
    model = CatBoostClassifier(
        iterations=500,
        depth=6,
        learning_rate=0.05,
        auto_class_weights='Balanced',
        random_seed=42,
        verbose=False
    )
    
    model.fit(X_train, y_train, eval_set=(X_val, y_val))
    return model

# Adicionar em main()
models['CatBoost'] = train_catboost(X_train, y_train, X_val, y_val)
```

---

## 🎯 Próximos Passos

### Após Melhorar o Modelo

1. **Validar em Produção**
   ```bash
   # Copiar modelo para resources
   cp modelo_melhorado.onnx src/main/resources/
   
   # Atualizar metadata.json
   # Atualizar threshold no código
   ```

2. **A/B Testing**
   - Rodar modelo antigo e novo em paralelo
   - Comparar métricas de negócio
   - Gradualmente migrar tráfego

3. **Monitorar Performance**
   - Accuracy em produção
   - Latência de inferência
   - Drift de dados

4. **Retreinamento**
   - Agendar retreinamento mensal
   - Validar antes de deploy
   - Manter histórico de modelos

---

## 📚 Recursos Adicionais

### Documentação
- [GUIA_MELHORIA_MODELO_ML.md](../docs/GUIA_MELHORIA_MODELO_ML.md) - Guia completo
- [XGBoost Docs](https://xgboost.readthedocs.io/)
- [LightGBM Docs](https://lightgbm.readthedocs.io/)
- [Optuna Docs](https://optuna.readthedocs.io/)

### Tutoriais
- [Kaggle: Feature Engineering](https://www.kaggle.com/learn/feature-engineering)
- [Kaggle: Intermediate ML](https://www.kaggle.com/learn/intermediate-machine-learning)

---

## 🐛 Troubleshooting

### Erro: "ModuleNotFoundError: No module named 'xgboost'"
```bash
pip install xgboost
```

### Erro: "ONNX conversion failed"
```bash
pip install --upgrade skl2onnx onnxruntime
```

### Modelo muito lento
- Reduzir `n_estimators`
- Reduzir `max_depth`
- Usar `tree_method='hist'` no XGBoost

### Overfitting (train >> test)
- Aumentar regularização
- Reduzir complexidade do modelo
- Coletar mais dados
- Feature selection

### Underfitting (train e test baixos)
- Adicionar mais features
- Aumentar complexidade do modelo
- Remover regularização excessiva

---

## 💡 Dicas

### Para Melhor Performance
1. Use validação temporal (não aleatória)
2. Faça feature engineering antes de tuning
3. Comece simples, aumente complexidade gradualmente
4. Monitore overfitting constantemente
5. Documente todas as decisões

### Para Produção
1. Teste modelo em staging primeiro
2. Monitore latência de inferência
3. Configure alertas de drift
4. Mantenha fallback para modelo antigo
5. Versione todos os modelos

---

## 🎓 Conclusão

Este script implementa as melhores práticas de ML para melhorar o modelo de churn de **64.88%** para **80%+ AUC-ROC**.

**Próximo Passo**: Execute `python scripts/improve_model.py --phase 1` e valide os resultados!

---

**Equipe DataBeats** | ChurnInsight ML
