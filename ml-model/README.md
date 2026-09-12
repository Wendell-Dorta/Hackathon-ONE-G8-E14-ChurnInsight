<p align="center">
  <img src="https://upload.wikimedia.org/wikipedia/commons/2/26/Spotify_logo_with_text.svg" width="250">
</p>
<h1 align="center">Projeto ChurnInsight – Spotify Dataset</h1>
<h2 font color=gree, align="center">🌟 Hackathon ONE II </h2>

## 📌 Visão Geral

O **Churn Insight** é um projeto desenvolvido no contexto do **Hackathon ONE II**, com o objetivo de criar uma solução baseada em **Data Science e Machine Learning** para **prever a probabilidade de cancelamento (churn)** de clientes em serviços por assinatura.

O projeto simula um cenário real de negócio aplicado a plataformas de streaming, como o **Spotify**, permitindo que empresas atuem de forma **proativa na retenção de clientes**, reduzindo perdas financeiras e aumentando o **Customer Lifetime Value (CLV)**.

---

## 🎯 Problema de Negócio

Empresas que operam sob o modelo de assinatura enfrentam desafios constantes relacionados ao churn, impactando diretamente receita, crescimento e sustentabilidade do negócio.

Antecipar clientes com maior risco de cancelamento possibilita:

- Priorizar ações estratégicas de retenção  
- Criar campanhas personalizadas e direcionadas  
- Reduzir custos com aquisição de novos clientes  

---

## 💡 Solução Proposta

Foi desenvolvido um **modelo preditivo de classificação binária**, capaz de indicar se um cliente possui **alta ou baixa probabilidade de cancelar** o serviço.

A solução contempla um pipeline completo de Data Science:

- Análise exploratória dos dados (EDA)  
- Engenharia de atributos (*feature engineering*)  
- Treinamento de modelos supervisionados  
- Avaliação com métricas adequadas ao problema de churn  
- Serialização e preparação do modelo para integração com backend  

---

## 📊 Dataset Utilizado

- **Fonte:** Kaggle  
- **Nome:** Spotify Dataset for Churn Analysis  
- **Variável alvo:** `is_churned`  
  - `0` → Cliente ativo  
  - `1` → Cliente cancelou  

🔗 [Acessar Dataset CSV](https://github.com/aluizr/Hackathon-ONE---Churn-clientes/blob/main/spotify_churn_dataset.csv)

---

## 🧠 Modelagem e Avaliação

Os modelos de Machine Learning foram implementados utilizando **scikit-learn**, incluindo:

- **Regressão Logística**
- **Random Forest**
- **Gradient Boosting**

O modelo selecionado foi a **Regressão Logística** com balanceamento via **SMOTE**, priorizando **Recall** como métrica principal, pois oferece alta interpretabilidade via coeficientes e **SHAP**, permitindo identificar com clareza os fatores que impulsionam o churn (como idade e tempo de uso) para ações preventivas estratégicas.

---

## 🛠️ Stack Tecnológico

### Linguagens & Ambiente
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg" width="36" title="Python"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="36" title="Java"/>&nbsp;
  <img src="https://upload.wikimedia.org/wikipedia/commons/d/d0/Google_Colaboratory_SVG_Logo.svg" width="36" title="Google Colab"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kaggle/kaggle-original.svg" width="36" title="Kaggle"/>
</p>

### Data Science & Machine Learning
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/pandas/pandas-original.svg" width="36" title="Pandas"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/numpy/numpy-original.svg" width="36" title="NumPy"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/scikitlearn/scikitlearn-original.svg" width="36" title="Scikit-Learn"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/matplotlib/matplotlib-original.svg" width="36" title="Matplotlib"/>&nbsp;
  <img src="https://raw.githubusercontent.com/mwaskom/seaborn/master/doc/_static/logo-wide-lightbg.svg" width="60" title="Seaborn"/>&nbsp;
  <img src="https://joblib.readthedocs.io/en/latest/_static/joblib_logo.svg" width="36" title="Joblib"/>&nbsp;
  <img src="https://cdn.simpleicons.org/onnx" width="36" title="ONNX"/>&nbsp;
  <img src="https://raw.githubusercontent.com/microsoft/onnxruntime/main/docs/images/ONNX_Runtime_icon.png" width="36" title="ONNX Runtime"/>&nbsp;
  <img src="https://cdn-icons-png.flaticon.com/512/2621/2621215.png" width="36" title="Joblib (Model Persistence)"/>&nbsp;
  <img src="https://cdn-icons-png.flaticon.com/512/1041/1041916.png" width="36" title="Imbalanced-Learn (SMOTE)"/>&nbsp;
  <img src="https://cdn-icons-png.flaticon.com/512/2103/2103633.png" width="36" title="Phi-K Correlation"/>&nbsp;
</p>

### Front-End & Visualização
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/vitejs/vitejs-original.svg" width="40" title="Vite"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg" width="40" title="React"/>&nbsp;
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-original-wordmark.svg" width="40" title="Tailwind CSS"/>&nbsp;
  <img src="https://skillicons.dev/icons?i=lucide" width="40" title="Lucide React"/>&nbsp;
  <img src="https://skillicons.dev/icons?i=chartjs" width="40" title="Chart.js / Recharts"/>
</p>

### Back-End
<p align="left">
  <img src="https://cdn.simpleicons.org/springboot" width="36" title="Spring Boot"/>&nbsp;
  <img src="https://cdn.simpleicons.org/springsecurity" width="36" title="Spring Security"/>&nbsp;
  <img src="https://cdn.simpleicons.org/spring" width="36" title="Spring Framework"/>&nbsp;
  <img src="https://cdn.simpleicons.org/flyway" width="36" title="Flyway"/>&nbsp;
  <img src="https://img.shields.io/badge/Java-Lombok-EA1E2D?style=flat-square&logo=java&logoColor=white" title="Lombok"/>
</p>

### Banco de Dados
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="36" title="MySQL"/>
</p>

### Testes
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/junit/junit-original.svg" width="36" title="JUnit 5"/>
</p>

### Versionamento & Repositório
<p align="left">
  <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg" width="36" title="Git"/>&nbsp;
  <img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="36" title="GitHub">
</p>
---

##  ⚠️ Limitações do Estudo
- Dataset sintético
- Ausência de histórico temporal
- Variáveis comportamentais agregadas

---

## 📦 Próximos Passos

- Integração completa com API REST  
- Implementação do endpoint `/predict`  
- Persistência das previsões no banco de dados  
- Desenvolvimento de dashboard para visualização do risco de churn  

---
## 🔌 Contrato da API

### <font color=#1DB954> Endpoint
 - POST /predict
#
#### **Entrada (JSON)**

```json
{
    "endpoint": "POST /predict",
    "metadata": {
        "model_name": "Spotify Churn Model",
        "model_version": "1.0",
        "api_standard": "RESTful"
    },
    "payload_input": {
        "gender": "Other",
        "age": 27,
        "country": "US",
        "subscription_type": "Free",
        "listening_time": 284,
        "songs_played_per_day": 57,
        "skip_rate": 0.14,
        "device_type": "Desktop",
        "ads_listened_per_week": 41,
        "offline_listening": 0,
        "songs_per_minute": 0.2,
        "ad_intensity": 0.1025,
        "frustration_index": 5.880000000000001,
        "is_heavy_user": 1,
        "premium_no_offline": 0
    }
}

```
#### **Saída**

```json
{
        "prediction": "Não Vai Cancelar",
        "probability": 0.2556,
        "decision_threshold": 0.262755,
        "ai_diagnosis": {
            "primary_risk_factor": "Anúncios por Semana",
            "primary_retention_factor": "Uso Offline",
            "suggested_action": "Manter fluxo padrão"
        }
```

---
## 👥 Equipe do Projeto – Hackathon ONE II

### **Time Back-End**
- Ezandro Bueno &nbsp; <a href="https://github.com/ezbueno" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/ezandro-bueno-776aab192/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Jorge Filipi Dias &nbsp; <a href="https://github.com/JorgeFilipi" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/jfdias/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Wanderson Souza &nbsp; <a href="https://github.com/wandersonjafe" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/wandersonjafe/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Wendell Dorta &nbsp; <a href="https://github.com/Wendell-Dorta" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/wendell-dorta/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>

#

### **Time Data Science**
- André Ribeiro &nbsp; <a href="https://github.com/aluizr" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/andreluizr/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Kelly Muehlmann &nbsp; <a href="https://github.com/KellyMuehlmann" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/kelly-muehlmann-43b9962b3/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Luiz Alves &nbsp; <a href="https://github.com/lf-all" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/lfall/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>
- Mariana Fernandes &nbsp; <a href="https://github.com/marianafernandes2204" style="text-decoration: none;"><img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/Github-Dark.svg#gh-dark-mode-only" width="18" style="vertical-align: middle;"></a> &nbsp; | &nbsp; <a href="https://www.linkedin.com/in/mariana-fernandes-0a93a71b5/" style="text-decoration: none;"><img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/linkedin/linkedin-original.svg" width="18" style="vertical-align: middle;"/></a>

---
## 🔄 Status do Projeto

🚧 **MVP funcional em desenvolvimento**, evoluído durante o Hackathon ONE II (2026).

