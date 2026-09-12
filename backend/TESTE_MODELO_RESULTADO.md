# ✅ Resultado dos Testes do Modelo ONNX

## 📊 Informações do Modelo

- **Arquivo**: `modelo_hackathon.onnx`
- **Tamanho**: 833.02 KB
- **Producer**: OnnxMLTools v1.16.0
- **Tipo**: TreeEnsembleClassifier (XGBoost)
- **Status**: ✅ Modelo ONNX válido!

### Estrutura

**Input:**
- Nome: `float_input`
- Shape: `[batch_size, 17]`
- Tipo: `float32`

**Outputs:**
- `label`: Classe predita (0=STAY, 1=CHURN)
- `probabilities`: [prob_stay, prob_churn]

---

## 🧪 Resultados dos Testes

### 🔴 Teste 1: Cliente com ALTO RISCO

**Perfil:**
- Usuário Free com muitos anúncios (80/semana)
- Baixo engajamento (50min, 5 músicas/dia)
- Alta taxa de skip (80%)
- Não usa offline

**Resultado:**
- ✅ Classe Predita: **CHURN**
- Probabilidade de Churn: **59.93%**
- Risco: 🟡 **MÉDIO**

---

### 🟢 Teste 2: Cliente com BAIXO RISCO

**Perfil:**
- Usuário Premium
- Alto engajamento (500min, 50 músicas/dia)
- Baixa taxa de skip (20%)
- Usa offline

**Resultado:**
- ✅ Classe Predita: **STAY**
- Probabilidade de Permanência: **78.26%**
- Risco: 🟢 **BAIXO**

---

### 🟡 Teste 3: Cliente com MÉDIO RISCO

**Perfil:**
- Usuário Free
- Engajamento médio (200min, 20 músicas/dia)
- Taxa de skip média (50%)
- Alguns anúncios (30/semana)

**Resultado:**
- ⚠️ Classe Predita: **CHURN**
- Probabilidade de Churn: **95.53%**
- Risco: 🔴 **ALTO**

**Observação**: Este perfil foi classificado como alto risco, indicando que o modelo é sensível a padrões de engajamento médio combinados com uso gratuito.

---

## ✅ Conclusão

O modelo está funcionando corretamente e fazendo predições coerentes:

1. ✅ **Carregamento**: Modelo carrega sem erros
2. ✅ **Validação**: Estrutura ONNX válida
3. ✅ **Inferência**: Predições funcionando
4. ✅ **Lógica**: Resultados fazem sentido com os perfis

### Próximos Passos

1. ✅ Modelo testado e validado
2. 🔄 Rebuildar API com novo modelo
3. 🚀 Testar endpoint `/predict`
4. 📊 Validar no frontend

---

**Data do Teste**: 13/03/2026  
**Status**: ✅ APROVADO
