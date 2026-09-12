# 🔍 Como Visualizar o Modelo ONNX

## ✨ Forma Mais Fácil (Recomendada)

### 1. Abra o Netron Online

Clique aqui: **https://netron.app**

### 2. Arraste o Arquivo

Arraste este arquivo para o navegador:
```
src/main/resources/modelo_hackathon.onnx
```

### 3. Explore o Modelo

Você verá:
- 🌳 Grafo visual do modelo XGBoost
- 📥 Inputs: 17 features
- 📤 Outputs: probabilidades e classe
- ⚙️ Operações: TreeEnsembleClassifier

---

## 📊 Informações do Modelo (Via Script Python)

Execute:
```bash
python scripts/visualizar_onnx.py src/main/resources/modelo_hackathon.onnx
```

**Resultado:**
```
📁 Arquivo: modelo_hackathon.onnx
📏 Tamanho: 833.02 KB
🔢 Versão ONNX: 1
🏷️  Producer: OnnxMLTools
📝 Versão Producer: 1.16.0

📥 INPUTS:
  - float_input: [dynamic, 17] (FLOAT)

📤 OUTPUTS:
  - label: [dynamic] (INT64)
  - probabilities: [dynamic, 2] (FLOAT)

⚙️ OPERAÇÕES:
  - TreeEnsembleClassifier: 1x

✅ Modelo ONNX válido!
```

---

## 🎯 Resumo

| Método | Link/Comando | Vantagem |
|--------|--------------|----------|
| **Netron Online** | https://netron.app | ✅ Mais fácil, visual |
| **Python Script** | `python scripts/visualizar_onnx.py ...` | ✅ Informações técnicas |

---

**Recomendação**: Use o Netron Online - é super visual e não precisa instalar nada! 🚀
