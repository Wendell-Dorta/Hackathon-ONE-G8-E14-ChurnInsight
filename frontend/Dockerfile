# ==============================================================================
# BUILD - Node.js para compilar o React
# ==============================================================================
FROM node:20-alpine AS build
WORKDIR /app

# Instala dependências primeiro (aproveita cache do Docker)
COPY package*.json ./
RUN npm ci --only=production=false

# Copia código fonte
COPY . .

# Build para produção (VITE_* vars são embutidas no build)
ARG VITE_API_USERNAME=admin
ARG VITE_API_PASSWORD=Admin123
ENV VITE_API_USERNAME=$VITE_API_USERNAME
ENV VITE_API_PASSWORD=$VITE_API_PASSWORD

RUN npm run build

# ==============================================================================
# RUNTIME - Nginx leve para servir arquivos estáticos
# ==============================================================================
FROM nginx:alpine

# Copia configuração customizada do nginx (com proxy para API)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copia arquivos buildados
COPY --from=build /app/dist /usr/share/nginx/html

# Copia arquivos estáticos (clients.json, metrics.json)
COPY public/*.json /usr/share/nginx/html/

# Expõe porta 3000
EXPOSE 3000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1

CMD ["nginx", "-g", "daemon off;"]

