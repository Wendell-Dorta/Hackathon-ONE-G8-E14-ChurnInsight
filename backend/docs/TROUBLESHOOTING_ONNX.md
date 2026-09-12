# 🔧 Troubleshooting: Exportar XGBoost para ONNX

## ❌ Erro: "Unable to interpret 'device_type', feature names should follow pattern 'f%d'"

### Causa
XGBoost está usando nomes de features (strings) ao invés de índices numéricos. ONNX requer índices.

### Solução
Retreinar o modelo usando apenas arrays numpy (sem feature names):

```python
# Retreinar modelo SEM feature names
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

# Usar .values para remover feature names
model_for_onnx.fit(
    X_train_balanced.values,  # Remove feature names
    y_train_balanced.values,
    verbose=False
)

# Agora converter para ONNX
onx = onnxmltools.convert_xgboost(
    model_for_onnx,  # Usar modelo retreinado
    initial_types=initial_type,
    target_opset=12
)
```

**Importante**: O modelo retreinado terá a mesma performance, apenas sem feature names.

---

## ❌ Erro: "Unable to find a shape calculator"

### Causa
O `skl2onnx` não suporta XGBoost diretamente. Você precisa usar `onnxmltools`.

### Solução

**No Google Colab**, substitua a CÉLULA 9 por:

```python
# Instalar biblioteca correta
!pip install onnxmltools onnxruntime -q

import onnxmltools
from onnxmltools.convert.common.data_types import FloatTensorType
import onnxruntime as rt

print("💾 Exportando XGBoost para ONNX...")

# Definir input shape
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]

# Converter (usa onnxmltools, não skl2onnx!)
onx = onnxmltools.convert_xgboost(
    model, 
    initial_types=initial_type,
    target_opset=12
)

# Salvar
with open("modelo_xgboost.onnx", "wb") as f:
    f.write(onx.SerializeToString())

print("✅ Modelo exportado: modelo_xgboost.onnx")

# Testar
sess = rt.InferenceSession("modelo_xgboost.onnx")
input_name = sess.get_inputs()[0].name
test_sample = X_test.iloc[0:1].values.astype(np.float32)
pred_onnx = sess.run(None, {input_name: test_sample})

print(f"✅ Teste OK! Predição: {pred_onnx[1][0][1]:.4f}")

# Download
from google.colab import files
files.download('modelo_xgboost.onnx')
```

---

## ❌ Erro: "No module named 'onnxmltools'"

### Solução
```python
!pip install onnxmltools onnxruntime
```

---

## ❌ Erro: "ONNX model validation failed"

### Causa
Versão incompatível do ONNX.

### Solução
```python
# Usar target_opset compatível
onx = onnxmltools.convert_xgboost(
    model, 
    initial_types=initial_type,
    target_opset=12  # ou 11, 13
)
```

---

## ❌ Erro: "Input type mismatch"

### Causa
Tipo de dados incorreto na inferência.

### Solução
```python
# Converter para float32
test_sample = X_test.iloc[0:1].values.astype(np.float32)
pred_onnx = sess.run(None, {input_name: test_sample})
```

---

## ✅ Verificar se ONNX Funciona

```python
import onnxruntime as rt
import numpy as np

# Carregar modelo
sess = rt.InferenceSession("modelo_xgboost.onnx")

# Ver inputs/outputs
print("Inputs:", [i.name for i in sess.get_inputs()])
print("Outputs:", [o.name for o in sess.get_outputs()])

# Testar predição
input_name = sess.get_inputs()[0].name
test_data = np.random.rand(1, X_train.shape[1]).astype(np.float32)
result = sess.run(None, {input_name: test_data})

print("Predição:", result[1][0][1])  # Probabilidade de churn
```

---

## 📦 Dependências Corretas

```bash
pip install xgboost onnxmltools onnxruntime onnx
```

**Versões testadas**:
- xgboost >= 2.0.0
- onnxmltools >= 1.11.0
- onnxruntime >= 1.16.0
- onnx >= 1.14.0

---

## 🔄 Alternativa: Salvar como Pickle

Se ONNX continuar dando problema, você pode salvar como pickle:

```python
import pickle

# Salvar modelo
with open('modelo_xgboost.pkl', 'wb') as f:
    pickle.dump(model, f)

# Carregar modelo
with open('modelo_xgboost.pkl', 'rb') as f:
    model_loaded = pickle.load(f)

# Usar
pred = model_loaded.predict_proba(X_test)
```

**Desvantagem**: Pickle não funciona no backend Java. ONNX é necessário para produção.

---

## 🎯 Checklist de Validação

Após exportar, verifique:

- [ ] Arquivo `modelo_xgboost.onnx` foi criado
- [ ] Tamanho do arquivo > 0 bytes
- [ ] Teste de inferência funciona
- [ ] Predições ONNX == Predições XGBoost (±0.001)

```python
# Comparar predições
pred_xgb = model.predict_proba(X_test)[:, 1]
pred_onnx = sess.run(None, {input_name: X_test.values.astype(np.float32)})[1][:, 1]

diff = np.abs(pred_xgb - pred_onnx).mean()
print(f"Diferença média: {diff:.6f}")  # Deve ser < 0.001
```

---

## 📞 Ainda com Problemas?

1. Verifique versões das bibliotecas
2. Tente target_opset diferente (11, 12, 13)
3. Use dados sintéticos para isolar o problema
4. Consulte: https://github.com/onnx/onnxmltools/issues

---

**Equipe DataBeats** | ChurnInsight
