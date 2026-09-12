# Relatorio de Execucao Local - ChurnInsight API

Data: 2026-03-12
Ambiente validado: Windows 11 + Docker Desktop + PowerShell

## Objetivo

Documentar, de forma reproduzivel, o que foi diagnosticado e alterado para a aplicacao subir localmente com sucesso.

## Problemas Encontrados

1. Ausencia do arquivo `.env` na raiz do repositorio.
- Efeito: variaveis obrigatorias do Docker Compose ficavam vazias.
- Sintoma observado: falha em `docker compose config` com erro relacionado a portas/variaveis (ex.: `invalid proto`).

2. Docker Engine inicialmente indisponivel.
- Efeito: Compose nao conseguia iniciar containers.
- Sintoma observado: erro de conexao no pipe `dockerDesktopLinuxEngine`.

3. Conflito de porta do MySQL no host (`3306`).
- Efeito: container `db` nao conseguia subir.
- Causa: processo local `mysqld` ja ocupava a porta `3306`.

4. Compose principal sem `build` no servico `app`.
- Efeito: `docker compose up --build` nao construia a imagem do backend localmente quando necessario.

## Ajustes Aplicados

### 1) Criacao do arquivo `.env` (local de desenvolvimento)

Foi criado um `.env` na raiz com valores de dev para destravar compose e healthchecks.

Principais variaveis usadas:

- `DB_NAME=churn_db`
- `DB_ROOT_PASSWORD=root123`
- `DB_USER=churn_user`
- `DB_PASSWORD=churn123`
- `DB_PORT=3307`
- `APP_PORT=10808`
- `SECURITY_USER=admin`
- `SECURITY_PASSWORD=admin`
- `SECURITY_ROLES=ADMIN`
- `PROMETHEUS_PORT=9090`
- `GRAFANA_PORT=3001`
- `GF_ADMIN_PASSWORD=admin`
- `MYSQL_ZABBIX_PASSWORD=zabbix123`
- `MYSQL_ROOT_ZABBIX_PASSWORD=rootzbx123`
- `ZABBIX_WEB_PORT=8081`
- `VITE_API_USERNAME=admin`
- `VITE_API_PASSWORD=admin`

Observacao:
- Esses valores sao de desenvolvimento local.
- Nao devem ser usados em producao.

### 2) Ajuste no `docker-compose.yml` para build do backend

No servico `app`, foi adicionado:

```yaml
build:
  context: .
  dockerfile: Dockerfile
```

Resultado:
- `docker compose up --build` passou a construir a imagem da API local (`churninsight/churn-api:local`).

### 3) Ajuste no `docker-compose.yml` para porta do MySQL configuravel

No servico `db`, foi alterado:

De:
```yaml
ports:
  - "3306:3306"
```

Para:
```yaml
ports:
  - "${DB_PORT:-3306}:3306"
```

Resultado:
- Foi possivel usar `DB_PORT=3307` e evitar conflito com o MySQL nativo da maquina.

## Validacoes Executadas

### Toolchain local

- Java: OK (`21.0.10`)
- Maven Wrapper: OK
- Node: OK (`v22.22.0`)
- NPM: OK (`10.9.4`)

### Build local (fora do Docker)

- Backend: `./mvnw.cmd -DskipTests package` -> `BUILD SUCCESS`
- Frontend: `npm run build` -> build concluido com sucesso

### Docker Compose

Comando utilizado:

```bash
docker compose up -d --build db app frontend
```

Status final esperado (e validado):

- `db` -> healthy
- `app` -> healthy
- `frontend` -> healthy

Portas finais:

- Frontend: `http://localhost:3000`
- API: `http://localhost:10808`
- MySQL (container db no host): `localhost:3307`

### Smoke tests funcionais

1. Frontend
- `GET http://localhost:3000` -> HTTP 200

2. Health da API
- `GET http://localhost:10808/actuator/health` (com Basic Auth) -> `status: UP`

3. Swagger
- `GET http://localhost:10808/swagger-ui/index.html` (com Basic Auth) -> HTTP 200

4. Predicao real
- `POST http://localhost:10808/predict` com `example-predict-request.json` -> resposta valida (probabilidade, risco e diagnostico)

## Como Reproduzir Rapidamente

1. Garanta Docker Desktop ativo.
2. Na raiz do projeto, mantenha o arquivo `.env` com os valores de desenvolvimento.
3. Suba os servicos:

```bash
docker compose up -d --build db app frontend
```

4. Valide:

```bash
docker compose ps
```

5. Acesse:

- Frontend: `http://localhost:3000`
- API/Swagger: `http://localhost:10808/swagger-ui/index.html`

## Comandos de Operacao

Subir stack principal:

```bash
docker compose up -d --build db app frontend
```

Ver status:

```bash
docker compose ps
```

Ver logs da API:

```bash
docker compose logs -f app
```

Parar stack:

```bash
docker compose down
```

Parar stack e remover volume do MySQL (reset de dados locais):

```bash
docker compose down -v
```

## Observacoes Importantes

1. O warning `attribute version is obsolete` no Compose nao impede a execucao, mas pode ser limpo no futuro removendo `version` do arquivo.
2. O frontend foi validado no modo oficial da stack (Nginx + proxy), conforme recomendacao do projeto.
3. Se a porta `3000` ou `10808` estiver ocupada, ajuste no compose/env conforme necessidade da maquina.

## Resumo Executivo

A aplicacao passou a funcionar localmente apos:

1. Definicao de variaveis de ambiente no `.env`.
2. Correcao de conflito de porta do MySQL via `DB_PORT=3307`.
3. Habilitacao de build local do backend no `docker-compose.yml`.

Com isso, backend, banco e frontend subiram com health checks positivos e endpoint de predicao validado ponta a ponta.
