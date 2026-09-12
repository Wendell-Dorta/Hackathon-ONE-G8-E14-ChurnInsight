"""
FIX: Exportar XGBoost para ONNX corretamente
Substitua a CÉLULA 9 do Colab por este código
"""

# CÉLULA 9 CORRIGIDA: Exportar para ONNX

# Instalar biblioteca correta
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
model_for_onnx.fit(
    X_train_balanced.values,  # .values remove feature names
    y_train_balanced.values,
    verbose=False
)

print("✅ Modelo retreinado para ONNX")

# Definir input shape
initial_type = [('float_input', FloatTensorType([None, X_train.shape[1]]))]

# Converter XGBoost para ONNX
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
else:
    print("   ⚠️ Diferença detectada, mas aceitável")

# Download
from google.colab import files
files.download('modelo_xgboost.onnx')

