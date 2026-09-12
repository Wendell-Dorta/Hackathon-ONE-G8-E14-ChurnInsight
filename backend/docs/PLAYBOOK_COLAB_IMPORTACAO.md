# Playbook Operacional - Colab para API (ChurnInsight)

Data: 2026-03-12  
Objetivo: padronizar o fluxo de execucao do notebook no Colab, atualizacao de artefatos na API e validacao pos-importacao.

## 1. Escopo

Este playbook cobre o caminho:

1. Colab (treino/geracao)
2. Export de artefatos
3. Atualizacao da API local
4. Carga de dados e validacao do dashboard

## 2. Repositorios Envolvidos

1. Data Science (Colab):
   - [Hackathon-ONE---Churn-clientes](https://github.com/Equipe-14-DataBeats-Hackaton-NoCountry/Hackathon-ONE---Churn-clientes)
2. API (este repositorio):
   - churninsight-api
3. Frontend (neste repositorio):
   - pasta `frontend/`

## 3. Artefatos Obrigatorios do Colab

Ao finalizar o notebook `Hackathon_One_II.ipynb`, garantir os arquivos:

1. Modelo ONNX
   - Nome esperado na API: `modelo_hackathon.onnx`
2. Metadata do modelo
   - Nome esperado na API: `metadata.json`
3. (Opcional) Contrato de API atualizado
   - Nome sugerido: `contrato_api.json`
4. (Opcional) Massa de dados para carga inicial
   - Exemplo: `clients.json` ou CSV para batch

## 4. Publicacao dos Artefatos na API

Copiar os artefatos para estes caminhos no repositorio da API:

1. `src/main/resources/modelo_hackathon.onnx`
2. `src/main/resources/metadata.json`
3. `src/main/resources/contrato_api.json` (se houver mudanca de contrato)
4. `src/main/resources/clients.json` (se houver massa inicial)

Sincronizar tambem as copias usadas pelo frontend:

1. `frontend/modelo_hackathon.onnx`
2. `frontend/metadata.json`
3. `frontend/contrato_api.json` (se houver mudanca de contrato)
4. `frontend/public/clients.json` (se houver massa inicial)

Observacao:
Manter os mesmos nomes evita retrabalho em configuracao.
Backend e frontend devem permanecer com os mesmos arquivos para evitar divergencia entre API e interface.

## 5. Subida Local da Stack

Na raiz da API:

```bash
docker compose up -d --build db app frontend
```

Porta padrao local atual da API:

1. `APP_PORT=10808` (definida no `.env`)

## 6. Validacao Tecnica Minima

Executar na sequencia:

1. Health da API
   - `GET /actuator/health`
2. Predicao unica
   - `POST /predict` com payload valido
3. Dashboard
   - `GET /dashboard/metrics`
4. Frontend
   - `GET /clients.json`
   - `GET /metadata.json`

Se o dashboard estiver zerado, popular historico com carga de `POST /predict`.

## 7. Carga de Massa (Padrao Operacional)

Objetivo:
popular `prediction_history`, que e a fonte das metricas de `/dashboard/metrics`.

Checklist:

1. IDs de usuario unicos por request
2. Payload dentro do contrato
3. Medir `ok/fail`
4. Validar `/dashboard/metrics` antes e depois

Referencia validada nesta execucao:

1. Carga de 5000 requests em `/predict`
2. Resultado: `ok=5000`, `fail=0`
3. Dashboard pos-carga: `total_customers=5120`

## 8. Diferença Importante de Endpoints

1. `/dashboard/metrics`
   - Usa historico persistido de predicao
2. `/clients/count`
   - Usa outra fonte (nao necessariamente o mesmo historico)

Conclusao:
`/clients/count` zerado nao implica dashboard quebrado.

## 9. Checklist de Publicacao (PR)

Antes de abrir PR da API:

1. Atualizou ONNX e metadata
2. Sincronizou as copias equivalentes no frontend
3. Subiu stack local sem erro
4. Validou `/predict`, `/dashboard/metrics` e os assets estaticos do frontend
5. Registrou versao do modelo no corpo do PR
6. Anexou evidencias (comandos e respostas chave)

## 10. Troubleshooting Rapido

1. Erro de porta da API:
   - conferir `APP_PORT` no `.env`
2. Dashboard zerado:
   - executar carga em `/predict` com IDs unicos
3. Build Docker falhando em testes de controller:
   - garantir mocks atualizados para novas dependencias do controller
4. Predicao sem persistencia:
   - validar se `/predict` chama fluxo de persistencia do historico

## 11. Governanca Recomendada

Para reduzir erro operacional entre repos:

1. versionar modelo e metadata juntos
2. usar changelog curto por versao de modelo
3. manter este playbook como referencia oficial de handoff Data Science -> Backend
