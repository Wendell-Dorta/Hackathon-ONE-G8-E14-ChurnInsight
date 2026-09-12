# 🚀 Solução Rápida - Card Receita em Risco Vazio

## O que foi corrigido?

Foram aplicadas 3 correções no código:

1. ✅ Query SQL otimizada no repositório
2. ✅ Proteção contra dados vazios no serviço
3. ✅ Bug corrigido no hook do frontend

## Como aplicar as correções?

### Opção 1: Reiniciar com Docker (Recomendado)

```bash
# Parar os containers
docker-compose down

# Reconstruir e iniciar
docker-compose up --build -d

# Verificar logs
docker-compose logs -f backend
```

### Opção 2: Compilar Manualmente

```bash
# Backend
mvn clean package -DskipTests

# Frontend
cd frontend
npm run build
cd ..

# Reiniciar aplicação
```

## Verificação Rápida

### 1. Verificar se há dados no banco

```bash
# Conectar ao MySQL (ajuste as credenciais)
docker exec -it <container-mysql> mysql -u root -p

# Dentro do MySQL
USE churninsight;
SELECT COUNT(*) FROM churn_history;
```

**Se retornar 0**: Você precisa fazer predições primeiro!

### 2. Fazer uma predição de teste

Use o arquivo `example-predict-request.json`:

```bash
curl -X POST http://localhost:10808/predict \
  -H "Content-Type: application/json" \
  -u usuario:senha \
  -d @example-predict-request.json
```

### 3. Verificar o endpoint

```bash
curl -u usuario:senha http://localhost:10808/dashboard/metrics | jq .revenue_at_risk
```

**Resultado esperado**: Um número (pode ser 0.0 se todos forem Free)

### 4. Acessar o dashboard

Abra o navegador em `http://localhost:5173` (ou porta configurada) e verifique o card.

## Ainda está vazio?

Execute o diagnóstico completo:

```bash
# Executar script SQL de teste
docker exec -it <container-mysql> mysql -u root -p churninsight < docs/SQL_TESTE_RECEITA_EM_RISCO.sql
```

Consulte `docs/DIAGNOSTICO_RECEITA_EM_RISCO.md` para mais detalhes.

## Causa Mais Comum

**Tabela vazia!** Se você acabou de iniciar o projeto, não há predições salvas ainda.

**Solução**: Use o formulário de predição individual ou o upload em lote para popular dados.

## Valores Esperados

Se você tiver 100 clientes no TOP 25% distribuídos assim:
- 50 Premium (R$ 23,90) = R$ 1.195,00
- 30 Family (R$ 40,90) = R$ 1.227,00
- 20 Student (R$ 12,90) = R$ 258,00

**Total**: R$ 2.680,00 em risco

## Dúvidas?

Veja a documentação completa em:
- `docs/DIAGNOSTICO_RECEITA_EM_RISCO.md` - Diagnóstico detalhado
- `docs/RESUMO_CORRECOES_RECEITA.md` - Resumo das correções
- `docs/SQL_TESTE_RECEITA_EM_RISCO.sql` - Script de teste
