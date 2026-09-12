# 🔍 Como Visualizar Arquivos ONNX

## 📋 Índice

1. [Netron (Recomendado)](#netron)
2. [Python Script](#python-script)
3. [ONNX Runtime](#onnx-runtime)
4. [VS Code Extension](#vscode)

---

## 1️⃣ Netron (Recomendado - Mais Visual)

### Online (Sem Instalar)

**Mais fácil e rápido:**

1. Acesse: https://netron.app
2. Arraste o arquivo `modelo_hackathon.onnx` para o navegador
3. Explore o grafo visual do modelo

**Vantagens:**
- ✅ Não precisa instalar nada
- ✅ Interface visual intuitiva
- ✅ Mostra todas as camadas e conexões
- ✅ Exibe shapes e tipos de dados

### Desktop (Para Arquivos Grandes)

```bash
# Instalar via npm
npm install -g netron

# Abrir modelo
netron src/main/resources/modelo_hackathon.onnx
```

Ou baixar o instalador: https://github.com/lutzroeder/netron/releases

---

## 2️⃣ Python Script (Informações Detalhadas)

### Instalar Dependências

```bash
pip install onnx onnxruntime
```

### Usar o Script

```bash
# No diretório do projeto
python scripts/visualizar_onnx.py src/main/resources/modelo_hackathon.onnx
```

**Saída esperada:**

```
============================================================
📊 ANÁLISE DO MODELO ONNX
============================================================

📁 Arquivo: modelo_hackathon.onnx
📏 Tamanho: 853.01 KB
🔢 Versão ONNX: 12
🏷️  Producer: xgboost
📝 Versão Producer: 1.7.0

============================================================
🔗 ESTRUTURA DO GRAFO
============================================================

Nome: xgboost_model
Nós (operações): 150

============================================================
📥 INPUTS DO MODELO
============================================================

1. float_input
   Shape: [dynamic, 21]
   Tipo: FLOAT

============================================================
📤 OUTPUTS DO MODELO
============================================================

1. output_probability
   Shape: [dynamic, 2]
   Tipo: FLOAT

2. output_label
   Shape: [dynamic]
   Tipo: INT64

============================================================
⚙️  OPERAÇÕES DO MODELO
============================================================

  TreeEnsembleClassifier: 1x
  Identity: 2x
  Cast: 1x

============================================================
⚖️  PARÂMETROS DO MODELO
============================================================

Total de tensores inicializados: 5
Total de parâmetros: 45,678
Tamanho estimado: 0.17 MB (float32)

============================================================
✅ VALIDAÇÃO
============================================================

✅ Modelo ONNX válido!
```

---

## 3️⃣ ONNX Runtime (Testar Inferência)

### Script de Teste

Crie `scripts/testar_onnx.py`:

```python
import onnxruntime as rt
import numpy as np

# Carregar modelo
sess = rt.InferenceSession("src/main/resources/modelo_hackathon.onnx")

# Ver inputs
print("📥 Inputs:")
for input in sess.get_inputs():
    print(f"  - {input.name}: {input.shape} ({input.type})")

# Ver outputs
print("\n📤 Outputs:")
for output in sess.get_outputs():
    print(f"  - {output.name}: {output.shape} ({output.type})")

# Testar com dados fake
input_name = sess.get_inputs()[0].name
fake_data = np.random.rand(1, 21).astype(np.float32)

# Fazer predição
result = sess.run(None, {input_name: fake_data})

print("\n🎯 Resultado do teste:")
print(f"  Probabilidades: {result[0]}")
print(f"  Classe predita: {result[1]}")
```

**Executar:**

```bash
python scripts/testar_onnx.py
```

---

## 4️⃣ VS Code Extension

### Instalar Extensão

1. Abra VS Code
2. Vá em Extensions (Ctrl+Shift+X)
3. Procure por "ONNX Viewer"
4. Instale a extensão

### Usar

1. Abra o arquivo `.onnx` no VS Code
2. Clique com botão direito → "Open with ONNX Viewer"
3. Visualize o grafo do modelo

---

## 5️⃣ Comparar Modelos

### Script para Comparar

```python
import onnx

def comparar_modelos(modelo1_path, modelo2_path):
    m1 = onnx.load(modelo1_path)
    m2 = onnx.load(modelo2_path)
    
    print("Modelo 1:")
    print(f"  Nós: {len(m1.graph.node)}")
    print(f"  Parâmetros: {len(m1.graph.initializer)}")
    
    print("\nModelo 2:")
    print(f"  Nós: {len(m2.graph.node)}")
    print(f"  Parâmetros: {len(m2.graph.initializer)}")

# Comparar
comparar_modelos(
    "src/main/resources/modelo_hackathon_1.onnx",  # Antigo
    "src/main/resources/modelo_hackathon.onnx"     # Novo
)
```

---

## 🎯 Resumo Rápido

| Ferramenta | Uso | Vantagem |
|------------|-----|----------|
| **Netron Online** | Visualização rápida | Não precisa instalar |
| **Netron Desktop** | Arquivos grandes | Mais rápido |
| **Python Script** | Análise detalhada | Informações técnicas |
| **ONNX Runtime** | Testar inferência | Validar funcionamento |
| **VS Code** | Desenvolvimento | Integrado ao editor |

---

## 📊 Informações do Nosso Modelo

### Modelo Atual (XGBoost)

```
Arquivo: modelo_hackathon.onnx
Tamanho: ~853 KB
Tipo: TreeEnsembleClassifier
Inputs: [batch_size, 21] features
Outputs: 
  - Probabilidades: [batch_size, 2]
  - Classe: [batch_size]
```

### Features (21 no total)

**Numéricas (17):**
- age, listening_time, songs_played_per_day, skip_rate
- ads_listened_per_week, offline_listening, songs_per_minute
- ad_intensity, frustration_index, is_heavy_user
- premium_no_offline, engagement_score, frustration_index_v2
- premium_value, high_skip_low_time, free_heavy_ads, songs_per_hour

**Categóricas (4):**
- gender, country, subscription_type, device_type

---

## 🐛 Troubleshooting

### Erro: "No module named 'onnx'"

```bash
pip install onnx onnxruntime
```

### Erro: "File too large" no Netron Online

Use a versão desktop:

```bash
npm install -g netron
netron modelo_hackathon.onnx
```

### Erro: "Invalid ONNX model"

Valide o modelo:

```python
import onnx

modelo = onnx.load("modelo_hackathon.onnx")
onnx.checker.check_model(modelo)
print("✅ Modelo válido!")
```

---

## 📚 Recursos Adicionais

- **Netron**: https://github.com/lutzroeder/netron
- **ONNX Docs**: https://onnx.ai/
- **ONNX Runtime**: https://onnxruntime.ai/
- **ONNX Python**: https://github.com/onnx/onnx

---

**Equipe DataBeats** | ChurnInsight  
**Data**: Março 2026
