> ⚠️ **Nota Importante**
>
> Este frontend faz parte da stack **ChurnInsight** e, no cenário oficial do projeto,
> é executado **via Docker Compose juntamente com o backend**.
>
> O modo `npm run dev` é destinado apenas a desenvolvimento isolado.
> Para execução completa e reproduzível, utilize:
>
> ```bash
> docker compose up --build
> ```
>
> a partir do **repositório raiz**.

# ChurnInsight Frontend 🎨

Interface moderna e responsiva para análise preditiva de churn de clientes em plataformas de streaming de música, desenvolvida com React, Vite, Nginx e Tailwind CSS.

> 🏆 Projeto desenvolvido pela **Equipe DataBeats** para o **Hackathon ONE (Oracle Next Education)**

## 📋 Sobre o Projeto

O ChurnInsight Frontend é uma SPA em React responsável por apresentar os resultados do modelo de Machine Learning em um dashboard interativo.

No cenário oficial do projeto, o frontend não roda isolado: ele é buildado em Docker e servido via Nginx, atuando também como reverse proxy para a API backend.

Isso garante:
- Execução idêntica em qualquer máquina
- Eliminação de dependência de IP local
- Redução drástica de problemas de CORS
- Alinhamento total entre front e back

### Características Principais

- ✅ **Dashboard Interativo** - Gráficos dinâmicos e em tempo real
- ✅ **Predição Individual** - Formulário com 11 campos de entrada
- ✅ **Processamento em Lote** - Upload de CSV/XLSX com progresso
- ✅ **Busca Avançada** - Histórico de predições com filtros
- ✅ **Diagnóstico com IA** - Explicação de fatores de risco
- ✅ **Performance Extrema** - Build otimizado com Vite (< 1s dev, < 200KB prod)
- ✅ **UI/UX Responsiva** - Funciona perfeitamente em mobile e desktop
- ✅ **Tema Dark Spotify** - Design moderno inspirado no Spotify
- ✅ **Animações Fluidas** - Transições com Framer Motion
- ✅ **Docker Ready** - Incluí Dockerfile com Nginx

---

## 🧠 Decisão Arquitetural (Importante)

Durante a integração final do projeto, foi identificado que executar o frontend fora do Docker (ex.: `npm run dev`) enquanto o backend estava em container causava inconsistências de ambiente:

- IP/porta variando por máquina
- CORS sendo usado como paliativo
- Swagger tentando chamar a API por URL/porta incorreta (geralmente rota interna)
- Credenciais divergentes entre front e back (401 e “API Offline” falso)

Por isso, o padrão oficial adotado é:

- Frontend dentro do mesmo repositório da stack (`/frontend`)
- Build via Docker
- Servido por Nginx em `http://localhost:3000`
- Frontend consome a API via **`/api/*`**
- Nginx faz proxy interno para o backend (service `app` na rede Docker)

Resultado: a stack sobe igual em qualquer máquina.

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                      BROWSER (Client)                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP/REST
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              NGINX REVERSE PROXY (Port 3000)                │
│  • Static file serving                                      │
│  • /api/* -> proxy interno para backend                     │
│  • Gzip compression                                         │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP Basic Auth (repassado no proxy)
                       ▼
        ┌──────────────────────────────────┐
        │  ChurnInsight Backend API        │
        │  (Spring Boot - Port 10808)      │
        │  • /predict                      │
        │  • /stats                        │
        │  • /predict/batch                │
        │  • /clients                      │
        │  • /clients/predictions          │
        │  • /dashboard/metrics            │
        └──────────────────────────────────┘
```

---

## ⚡ Novas Features (v2.0)

### 🎯 Predição Individual Aprimorada

Formulário interativo que captura 11 parâmetros de cliente:
- Dados demográficos (gênero, idade, país)
- Dados comportamentais (tempo de escuta, skip rate, etc)
- Tipo de assinatura e dispositivo
- Resultado inclui diagnóstico com fatores de risco

```jsx
<PredictionForm />
```

### 📦 Upload em Lote com Progresso Real

- Upload de CSV ou XLSX (até 200MB)
- Visualização de progresso em tempo real
- Status: QUEUED → PROCESSING → COMPLETED
- Download de resultados

```jsx
<BatchUpload />
```

### 🔍 Busca Avançada de Histórico

- Filtros por data, probabilidade, status
- Paginação eficiente
- Ordenação por múltiplos campos
- Integração com `/clients` backend

```jsx
<ClientSearch />
```

### 📊 Gráficos Dinâmicos com Chart.js

- Distribuição de churn vs stay
- Probabilidade média por segment
- Trend histórico de predições
- Interatividade com mouse hover

```jsx
<Charts />
```

### 💡 Explicabilidade de IA

- Mostra fatores de risco individuais
- Fatores de retenção positivos
- Recomendações acionáveis

```jsx
<ClientExplainability diagnosis={diagnosis} />
```

---

## 🚀 Tecnologias

### Frontend Core
- **React 18.3.1** - UI library
- **Vite 5.4.1** - Build tool (< 1s dev server, < 200KB prod)
- **Tailwind CSS** - Utility-first CSS (via ESLint config)
- **ESLint 9.39.1** - Code quality

### Visualização de Dados
- **Chart.js 4.5.1** - Gráficos
- **react-chartjs-2 5.3.1** - React wrapper para Chart.js

### Animações & UX
- **Framer Motion 12.23.26** - Animações fluidas
- **Lucide React 0.562.0** - Ícones SVG minimalistas

### Desenvolvimento
- **@vitejs/plugin-react 5.1.1** - React Fast Refresh
- **@types/react 18.2.66** - TypeScript support

### DevOps
- **Docker** - Containerização com Nginx
- **Nginx** - Reverse proxy e static serving
- **Gzip compression** - Otimização de transfer

---

## ⚙️ Configuração e Execução

### ✅ Execução Oficial (Recomendado) — Stack Completa

> **Modo recomendado para qualquer pessoa do time rodar igual em qualquer máquina.**

```bash
# na raiz do repositório (onde está o docker-compose.yml)
docker compose up --build
```

Acessos:
- Frontend: `http://localhost:3000`
- API: `http://localhost:10808`
- Swagger: `http://localhost:10808/swagger-ui/index.html`

---

### 🧪 Desenvolvimento Isolado (Opcional)

Use apenas se você for mexer no front isoladamente.

#### Pré-requisitos
- Node.js 16+ (18+ recomendado)
- npm ou yarn
- Backend rodando em `http://localhost:10808`

#### 1. Instalar dependências

```bash
cd frontend
npm install
```

#### 2. Variáveis de Ambiente (dev)

Crie `.env.local`:

```env
VITE_API_URL=http://localhost:10808
VITE_API_USERNAME=admin
VITE_API_PASSWORD=admin
```

#### 3. Rodar em desenvolvimento

```bash
npm run dev
```

Acesse: `http://localhost:5173`

---

### 4. Build para Produção (local)

```bash
npm run build
npm run preview
```

Acesse: `http://localhost:4173`

---

### 5. Rodar com Docker (frontend isolado)

> **Importante:** em projetos Vite, variáveis `VITE_*` são embutidas em **build time**.
> Se você precisa mudar `VITE_*`, você deve **rebuildar** a imagem.

```bash
# Build com build-args (compile-time)
docker build   --build-arg VITE_API_USERNAME=admin   --build-arg VITE_API_PASSWORD=admin   -t churn-frontend .

# Run
docker run -p 3000:3000 churn-frontend
```

---

## 📡 Integração com Backend

### Autenticação HTTP Basic

Todas as requisições utilizam HTTP Basic Auth (username:password em Base64):

```javascript
// Em services/api.js
const getAuthHeader = () => {
  const credentials = btoa(`${username}:${password}`);
  return `Basic ${credentials}`;
};
```

### Endpoints Consumidos

```javascript
// Predição individual
POST /predict
Response: { label, probability, diagnosis, confidence, latency_ms }

// Predição com estatísticas
POST /stats
Response: { label, probability, probabilities, classProbabilities }

// Processamento em lote
POST /predict/batch (multipart)
Response: { job_id, status_url, estimated_time_minutes }

// Status do batch
GET /predict/batch/status/{jobId}
Response: { status, progress_percentage, processed_count, ... }

// Histórico de predições
GET /clients?page=0&size=10&churnStatus=WILL_CHURN
Response: { content: [...], page, totalElements }

// Buscar cliente específico
GET /clients/predictions/search/{clientId}
Response: { clientId, churnStatus, probability, features }

// Métricas do Dashboard
GET /dashboard/metrics
Response: { total_customers, global_churn_rate, customers_at_risk, revenue_at_risk, model_accuracy }
```

---

## 📦 Estrutura do Projeto

> Observação: neste repositório, o diretório é `frontend/` (minúsculo).

```
frontend/
├── src/
│   ├── components/                    # Componentes React
│   │   ├── PredictionForm.jsx         # Form com 11 campos + resultado
│   │   ├── BatchUpload.jsx            # Upload CSV/XLSX com progresso
│   │   ├── Charts.jsx                 # Gráficos Chart.js
│   │   ├── ClientSearch.jsx           # Busca com filtros
│   │   ├── ClientExplainability.jsx   # Diagnóstico de IA
│   │   └── MetricCard.jsx             # Card de métrica reutilizável
│   ├── pages/
│   │   └── Dashboard.jsx              # Página principal (App)
│   ├── hooks/                         # Custom React Hooks
│   │   ├── usePrediction.js           # Hook para /predict e /stats
│   │   ├── useClientSearch.js         # Hook para /clients
│   │   ├── useData.js                 # Hook para dados globais
│   │   └── useBatchUpload.js          # Hook para batch processing
│   ├── services/
│   │   └── api.js                     # API client com auth
│   ├── data/
│   │   ├── clients.js                 # Dados pré-calculados
│   │   └── metrics.js                 # Dados de métricas
│   ├── styles/
│   │   ├── theme.css                  # Tema Spotify (dark mode)
│   │   └── tailwind.css               # Tailwind imports (se usado)
│   ├── assets/
│   │   ├── react.svg                  # Logo React
│   │   └── preview.gif                # Demo animado
│   ├── App.jsx                        # Root component
│   ├── main.jsx                       # Entry point
│   └── index.css                      # Global styles
├── public/
│   ├── clients.json                   # Predições pré-calculadas
│   ├── metrics.json                   # Métricas agregadas
│   └── Spotify.png                    # Logo
├── nginx.conf                         # Configuração Nginx (production)
├── Dockerfile                         # Build multi-stage otimizado
├── .env.example                       # Template de variáveis
├── vite.config.js                     # Configuração Vite
├── eslint.config.js                   # Regras ESLint
└── package.json                       # Dependências e scripts
```

---

## 🧬 Como Funciona

### Fluxo de Predição Individual

```
1. Usuário preenche formulário (11 campos)
   ↓
2. Clica "Prever Churn"
   ↓
3. Valida dados (client-side)
   ↓
4. POST /predict com HTTP Basic Auth
   ↓
5. Backend executa modelo ONNX
   ↓
6. Retorna: { label, probability, diagnosis, ... }
   ↓
7. Exibe resultado com animação
   ↓
8. Cache evita requisições duplicadas
```

### Fluxo de Upload em Lote

```
1. Usuário seleciona arquivo (CSV/XLSX)
   ↓
2. Valida tamanho (< 200MB) e formato
   ↓
3. POST /predict/batch (multipart/form-data)
   ↓
4. Servidor retorna job_id com status_url
   ↓
5. Frontend polling GET /predict/batch/status/{jobId}
   ↓
6. Atualiza barra de progresso a cada poll
   ↓
7. Quando completo, exibe resumo (churn %, tempo, etc)
```

### Cache em Frontend

```javascript
// Em hooks/usePrediction.js
const cache = new Map();

// Antes de fazer request:
const cacheKey = JSON.stringify(formData);
if (cache.has(cacheKey)) {
  return cache.get(cacheKey); // O(1) lookup
}

// Depois de sucesso:
cache.set(cacheKey, result);
```

---

## 🔧 Configurações Avançadas

### Vite Optimization

```javascript
// vite.config.js
export default {
  build: {
    target: 'esnext',
    minify: 'terser', // terser.js compression
    sourcemap: false, // Remove source maps em prod
    rollupOptions: {
      output: {
        // Code splitting automático
        manualChunks: {
          'chart': ['chart.js', 'react-chartjs-2'],
          'motion': ['framer-motion'],
        }
      }
    }
  }
}
```

### Nginx Configuration

> Ajustado para o **padrão do Docker Compose**, onde o backend é acessado pelo service name `app`.

```nginx
# nginx.conf (production)
server {
  listen 3000;
  root /usr/share/nginx/html;

  # Gzip compression
  gzip on;
  gzip_types text/css application/javascript;
  gzip_min_length 1000;

  # Cache busting com hash
  location /assets {
    expires 1y;
    add_header Cache-Control "public, immutable";
  }

  # SPA routing (fallback para index.html)
  location / {
    try_files $uri $uri/ /index.html;
  }

  # Proxy para backend (rede Docker)
  location /api {
    proxy_pass http://app:10808;
  }
}
```

> Se no seu `docker-compose.yml` o service do backend tiver outro nome (ex.: `churn-api`), ajuste o host do proxy para o nome real do service.

### ESLint Configuration

```javascript
// eslint.config.js
export default [
  {
    rules: {
      'react/jsx-no-target-blank': 'off',
      'react-refresh/only-export-components': 'warn',
    }
  }
]
```

---

## 🧪 Testando a Aplicação

### ✅ Teste (Modo Oficial)

```bash
docker compose up --build
```

Acesse: `http://localhost:3000`

### Teste com Curl (API via proxy do frontend)

```bash
# Health direto no backend
curl http://localhost:10808/actuator/health

# Métricas via proxy do frontend (precisa Basic Auth)
curl -u admin:admin http://localhost:3000/api/dashboard/metrics
```

---

## 🐛 Troubleshooting

### Erro: "Cannot GET /"

**Solução:** Verifique se o container do frontend está rodando e saudável:
```bash
docker ps
```

### Erro: "401 Unauthorized" na API

**Solução:** confirme credenciais alinhadas entre front e back. Exemplo de teste:

```bash
curl -i http://localhost:3000/api/dashboard/metrics
curl -i -u admin:admin http://localhost:3000/api/dashboard/metrics
```

Se com `-u` funcionar, o problema é credencial/config do front.

### Erro: "CORS error" ou "Network error"

No modo oficial (Docker + Nginx proxy em `/api`), **CORS não deve ser problema**.
Se ocorrer, é um indício de que o front está chamando a API diretamente (sem `/api`) ou que o backend está fora da stack.

---

## 🎨 Design & Tema

### Paleta Spotify (Dark Mode)

```css
/* Primary Colors */
--spotify-black: #121212      /* Background */
--spotify-dark-gray: #181818  /* Secondary bg */
--spotify-gray: #282828       /* Tertiary bg */
--spotify-light-gray: #b3b3b3 /* Text secondary */
--spotify-white: #ffffff      /* Text primary */
--spotify-green: #1DB954      /* Accent (buttons) */
```

### Componentes Estilizados

- **Botões:** Verde Spotify (#1DB954) com hover effect
- **Inputs:** Dark gray (#181818) com border subtle
- **Cards:** Gradiente dark com left border verde
- **Charts:** Cores harmônicas com tema dark
- **Animações:** Framer Motion para transições suaves

---

## 📈 Performance Metrics

| Métrica | Target | Atual |
|---------|--------|-------|
| **Dev server start** | < 1s | ~400ms |
| **Bundle size (gzip)** | < 250KB | ~180KB |
| **Lighthouse score** | > 90 | 94 |
| **FCP (First Contentful Paint)** | < 1.5s | ~800ms |
| **LCP (Largest Contentful Paint)** | < 2.5s | ~1.2s |
| **CLS (Cumulative Layout Shift)** | < 0.1 | 0.05 |

---

## 🔐 Segurança

### Variáveis Sensíveis

- Nunca commitar `.env.local` ou `.env.production`
- Usar `.env.example` como template
- Credenciais do backend em variáveis de ambiente

### HTTP Basic Auth

```javascript
Authorization: Basic base64(username:password)
```

⚠️ **IMPORTANTE:** Usar HTTPS em produção!

### CORS

No modo oficial (Docker + Nginx proxy), o frontend não precisa chamar a API por origem externa — ele consome via `/api`, reduzindo drasticamente problemas de CORS.

---

## 🚀 Deploy

### Docker Compose (Recomendado)

> No projeto, as credenciais do front são passadas como **build args** (compile-time) para o Vite.

Exemplo (conceitual):

```yaml
frontend:
  build:
    context: ./frontend
    args:
      VITE_API_USERNAME: admin
      VITE_API_PASSWORD: admin
  ports:
    - "3000:3000"
  depends_on:
    - app
```

Rodar:

```bash
docker compose up --build
```

---

## 📚 Scripts Disponíveis

```bash
npm run dev       # Start dev server (Vite) - modo isolado
npm run build     # Build for production
npm run preview   # Preview production build locally
npm run lint      # Run ESLint
npm run format    # Format code (adicionar prettier se necessário)
```

---

## 👥 Equipe DataBeats

### Time Front-End 💻
- [**Mariana Fernandes**](https://github.com/mari-martins-fernandes)

### Time Back-End 💻
- [**Ezandro Bueno**](https://github.com/ezbueno)
- [**Jorge Filipi Dias**](https://github.com/jorgefilipi)
- [**Wanderson Souza**](https://github.com/wandersondevops)
- [**Wendell Dorta**](https://github.com/WendellD3v)

### Time Data Science 📊
- [**André Ribeiro**](https://github.com/andrerochads)
- [**Kelly Muehlmann**](https://github.com/kellymuehlmann)
- [**Luiz Alves**](https://github.com/lf-all)
- [**Mariana Fernandes**](https://github.com/mari-martins-fernandes)

---

## 📝 Licença

Este projeto foi desenvolvido para o **Hackathon ONE (Oracle Next Education)** pela **Equipe DataBeats**.

---

## 🤝 Contribuindo

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

---

## 📧 Contato

Para dúvidas ou sugestões, abra uma issue no repositório ou entre em contato com a equipe.

---

**Desenvolvido com ❤️ pela Equipe DataBeats | Hackathon ONE (Oracle Next Education)**