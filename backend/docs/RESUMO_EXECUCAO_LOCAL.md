# Resumo de Execucao Local - ChurnInsight

Data: 2026-03-12
Status final: Aplicacao funcionando localmente (frontend + backend + banco)

## O que estava impedindo a execucao

1. Nao havia arquivo `.env` na raiz.
- Sem ele, o Docker Compose recebia variaveis vazias.

2. Havia conflito de porta no banco.
- A porta `3306` ja estava em uso por um MySQL local (`mysqld`).

3. O servico `app` no compose principal nao tinha bloco de `build`.
- Em alguns cenarios, `docker compose up --build` nao construia a imagem da API local.

## O que foi feito para funcionar

1. Criado `.env` de desenvolvimento na raiz.
- Variaveis essenciais de DB, seguranca e portas.
- Definido `DB_PORT=3307` para evitar conflito com MySQL local.

2. Ajustado `docker-compose.yml` no servico `db`.
- De `3306:3306` para `${DB_PORT:-3306}:3306`.

3. Ajustado `docker-compose.yml` no servico `app`.
- Adicionado bloco `build` com `context: .` e `dockerfile: Dockerfile`.

## Validacoes executadas

1. Build backend local: sucesso.
2. Build frontend local: sucesso.
3. Subida da stack principal com Docker Compose: sucesso.
4. Health checks dos containers: sucesso.
5. Frontend em `http://localhost:3000`: HTTP 200.
6. API em `http://localhost:10808/actuator/health`: status `UP`.
7. Swagger em `http://localhost:10808/swagger-ui/index.html`: HTTP 200.
8. Predicao real via endpoint `/predict`: resposta valida.

## Como subir (modo recomendado)

```bash
docker compose up -d --build db app frontend
```

## Como validar rapidamente

```bash
docker compose ps
```

Esperado:
- `db` healthy
- `app` healthy
- `frontend` healthy

## Endpoints finais

- Frontend: `http://localhost:3000`
- API: `http://localhost:10808`
- Swagger: `http://localhost:10808/swagger-ui/index.html`
- MySQL local da stack (host): `localhost:3307`

## Documento completo

Para detalhes completos (diagnostico, evidencias, comandos e operacao), consultar:
- `EXECUCAO_LOCAL_RELATORIO.md`
