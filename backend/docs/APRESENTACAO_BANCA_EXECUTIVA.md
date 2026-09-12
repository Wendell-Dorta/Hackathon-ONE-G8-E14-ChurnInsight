# ChurnInsight - Versao Banca de Avaliacao

Data: 2026-03-12  
Equipe: DataBeats  
Objetivo: comprovar prontidao tecnica da solucao para demonstracao e avaliacao final.

## 1. Contexto

Durante a preparacao para execucao local da plataforma, foram identificados bloqueios de ambiente que inviabilizavam a subida completa da stack em maquina de desenvolvimento.

## 2. Problema

A solucao nao iniciava de forma consistente por tres causas principais:

1. Variaveis obrigatorias ausentes (falta de arquivo .env).
2. Conflito de porta do banco (3306 em uso por MySQL local).
3. Inconsistencia de build do backend no compose principal.

## 3. Acao Corretiva

Foi executado um plano de estabilizacao com foco em reproducibilidade:

1. Criacao e padronizacao do .env de desenvolvimento.
2. Parametrizacao de porta de banco via DB_PORT com uso local em 3307.
3. Ajuste do compose para build local explicito do servico app.
4. Revalidacao ponta a ponta com testes funcionais reais.

## 4. Evidencias de Resultado

1. Frontend acessivel: http://localhost:3000
2. API acessivel: http://localhost:10808
3. Swagger acessivel: http://localhost:10808/swagger-ui/index.html
4. Health da API: status UP
5. Containers principais: healthy (db, app, frontend)
6. Predicao real executada com sucesso no endpoint /predict

## 5. Impacto para a Banca

1. Solucao pronta para demo sem dependencia de ajustes manuais no momento da apresentacao.
2. Reducao de risco operacional durante avaliacao.
3. Setup reproduzivel para qualquer membro da equipe.
4. Maior confianca na sustentacao tecnica da proposta.

## 6. Comando Oficial de Subida

```bash
docker compose up -d --build db app frontend
```

## 7. Conclusao

A plataforma ChurnInsight encontra-se operacional para apresentacao, com ambiente local estabilizado, validacao tecnica concluida e fluxo funcional confirmado ponta a ponta.
