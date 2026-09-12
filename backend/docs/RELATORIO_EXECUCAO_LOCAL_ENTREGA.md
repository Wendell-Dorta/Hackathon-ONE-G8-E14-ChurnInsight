# ChurnInsight

## Relatorio Oficial de Execucao Local

Equipe: DataBeats  
Projeto: ChurnInsight API + Frontend  
Data: 2026-03-12  
Ambiente validado: Windows 11, PowerShell, Docker Desktop

---

## Indice

1. [Resumo Executivo](#resumo-executivo)
2. [Problemas Identificados](#problemas-identificados)
3. [Correcoes Aplicadas](#correcoes-aplicadas)
4. [Validacoes Executadas](#validacoes-executadas)
5. [Como Reproduzir](#como-reproduzir)
6. [Comandos Operacionais](#comandos-operacionais)
7. [Endpoints Finais](#endpoints-finais)
8. [Conclusao](#conclusao)

---

## Resumo Executivo

A stack foi estabilizada para execucao local completa (frontend, backend e banco), com health checks positivos e teste real de predicao executado com sucesso.

## Problemas Identificados

1. Ausencia do arquivo `.env` na raiz.
2. Docker Engine indisponivel no inicio da verificacao.
3. Conflito de porta `3306` com MySQL local (`mysqld`).
4. Servico `app` no compose principal sem bloco de `build`.

## Correcoes Aplicadas

1. Criacao de `.env` de desenvolvimento com variaveis obrigatorias.
2. Definicao de `DB_PORT=3307` para evitar conflito local de porta.
3. Ajuste de `docker-compose.yml` no servico `db`:

```yaml
ports:
  - "${DB_PORT:-3306}:3306"
```

4. Ajuste de `docker-compose.yml` no servico `app` para build local:

```yaml
build:
  context: .
  dockerfile: Dockerfile
```

## Validacoes Executadas

1. Build backend local: `./mvnw.cmd -DskipTests package` (sucesso).
2. Build frontend local: `npm run build` (sucesso).
3. Subida da stack: `docker compose up -d --build db app frontend`.
4. Status dos servicos: `db`, `app` e `frontend` em `healthy`.
5. Frontend: `http://localhost:3000` (HTTP 200).
6. Health API: `http://localhost:10808/actuator/health` (`status: UP`).
7. Swagger: `http://localhost:10808/swagger-ui/index.html` (HTTP 200).
8. Predicao real em `/predict` com payload de exemplo (resposta valida).

## Como Reproduzir

1. Garantir Docker Desktop em execucao.
2. Manter o arquivo `.env` na raiz com configuracao de desenvolvimento.
3. Executar:

```bash
docker compose up -d --build db app frontend
```

4. Validar:

```bash
docker compose ps
```

## Comandos Operacionais

Subir stack:

```bash
docker compose up -d --build db app frontend
```

Logs da API:

```bash
docker compose logs -f app
```

Parar stack:

```bash
docker compose down
```

Resetar dados locais (remove volume DB):

```bash
docker compose down -v
```

## Endpoints Finais

- Frontend: `http://localhost:3000`
- API: `http://localhost:10808`
- Swagger: `http://localhost:10808/swagger-ui/index.html`
- MySQL exposto no host: `localhost:3307`

## Conclusao

A execucao local foi normalizada com ajustes de ambiente e compose, mantendo o comportamento esperado da arquitetura oficial do projeto.

Documentacao complementar:

- `RESUMO_EXECUCAO_LOCAL.md`
- `EXECUCAO_LOCAL_RELATORIO.md`
