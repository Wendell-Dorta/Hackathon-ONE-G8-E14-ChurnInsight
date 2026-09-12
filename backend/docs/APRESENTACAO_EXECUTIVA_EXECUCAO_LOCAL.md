# ChurnInsight - Resumo Executivo de Execucao Local

Data: 2026-03-12  
Equipe: DataBeats  
Escopo: Validacao operacional da stack local (frontend + backend + banco)

## Objetivo

Garantir que a plataforma ChurnInsight possa ser executada localmente de forma estavel, reproduzivel e pronta para demonstracao.

## Cenario Inicial

Durante a tentativa de subida da stack, foram encontrados bloqueios de ambiente que impediam a execucao completa:

1. Variaveis obrigatorias ausentes por falta de arquivo .env.
2. Inconsistencia na construcao do backend via compose em alguns cenarios.
3. Conflito de porta 3306 com MySQL local ja ativo na maquina.

## Acoes Executadas

1. Padronizacao de ambiente local via .env.
2. Ajuste no Docker Compose para build local explicito do backend.
3. Parametrizacao da porta do banco para evitar colisao no host.
4. Revalidacao ponta a ponta com health checks e chamada real de predicao.

## Resultado Tecnico

Status final: OPERACIONAL

- Frontend online: http://localhost:3000
- API online: http://localhost:10808
- Swagger online: http://localhost:10808/swagger-ui/index.html
- Banco local da stack: localhost:3307
- Health dos servicos: healthy
- Endpoint /predict: resposta valida

## Evidencias de Sucesso

1. Build backend: sucesso.
2. Build frontend: sucesso.
3. Docker compose: stack principal em execucao estavel.
4. Health API: status UP.
5. Teste funcional real de predicao: aprovado.

## Impacto para a Entrega

1. Ambiente local agora reproduzivel para todo o time.
2. Reducao de risco para demo e avaliacao final.
3. Menor tempo de setup para onboard de novos colaboradores.
4. Base pronta para evolucao de features sem bloqueio de infraestrutura.

## Recomendacoes

1. Manter o arquivo .env de desenvolvimento como referencia interna do time.
2. Preservar a parametrizacao de portas no compose para evitar conflitos entre maquinas.
3. Usar o comando oficial abaixo para subida:

```bash
docker compose up -d --build db app frontend
```

## Conclusao

A operacao local do ChurnInsight foi estabilizada com sucesso. A stack esta pronta para demonstracao, validacao funcional e continuidade do desenvolvimento.
