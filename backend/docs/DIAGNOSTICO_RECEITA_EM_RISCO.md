# Diagnóstico: Card "Receita Potencial em Risco" Vazio

## Problema
O card "Receita Potencial em Risco (Est.)" está aparecendo vazio no dashboard.

## Causa Raiz Identificada
A query SQL que calcula a receita em risco pode não estar retornando dados por uma das seguintes razões:

1. **Tabela vazia**: Não há predições salvas na tabela `churn_history`
2. **Query não retorna resultados**: A query do TOP 25% não está funcionando corretamente
3. **Dados sem subscription_type**: Os registros não têm o campo `subscription_type` preenchido
4. **Bug no frontend**: Hook `useData.js` tinha dois `return` statements (CORRIGIDO)

## Fluxo de Dados

```
Frontend (Dashboard.jsx)
  ↓
API (/dashboard/metrics)
  ↓
DashboardMetricsService.calculateRevenueAtRisk()
  ↓
PredictionHistoryRepository.getTop25SubscriptionCounts()
  ↓
Query SQL (TOP 25% por probabilidade)
```

## Alterações Realizadas

### 1. Correção na Query SQL
**Arquivo**: `PredictionHistoryRepository.java`

Adicionei `ORDER BY count DESC` e alias explícito para garantir ordenação consistente:

```sql
SELECT ranked.subscription_type, COUNT(*) as count
FROM (
    SELECT subscription_type,
           ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
           COUNT(*) OVER () as total_count
    FROM churn_history
) ranked
WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
GROUP BY ranked.subscription_type
ORDER BY count DESC
```

### 2. Proteção contra Lista Vazia
**Arquivo**: `DashboardMetricsService.java`

Adicionei verificação para evitar erro quando não há dados:

```java
if (top25ByPlan == null || top25ByPlan.isEmpty()) {
    return 0.0;
}
```

### 3. Correção no Hook useData
**Arquivo**: `frontend/src/hooks/useData.js`

Removido `return` duplicado que impedia o retorno de `fallbackActive`:

```javascript
// ANTES (ERRADO):
return { metrics, apiStatus, loading, error, refresh: fetchData };
return { metrics, apiStatus, loading, error, refresh: fetchData, fallbackActive };

// DEPOIS (CORRETO):
return { metrics, apiStatus, loading, error, refresh: fetchData, fallbackActive };
```

## Como Testar

### 1. Verificar se há dados na tabela
```sql
SELECT COUNT(*) FROM churn_history;
```

### 2. Verificar distribuição de subscription_type
```sql
SELECT subscription_type, COUNT(*) 
FROM churn_history 
GROUP BY subscription_type;
```

### 3. Testar a query do TOP 25%
```sql
SELECT ranked.subscription_type, COUNT(*) as count
FROM (
    SELECT subscription_type,
           ROW_NUMBER() OVER (ORDER BY probability DESC) as row_num,
           COUNT(*) OVER () as total_count
    FROM churn_history
) ranked
WHERE ranked.row_num <= CEIL(ranked.total_count * 0.25)
GROUP BY ranked.subscription_type
ORDER BY count DESC;
```

### 4. Verificar o endpoint da API
```bash
curl -u usuario:senha http://localhost:10808/dashboard/metrics
```

Procure pelo campo `revenue_at_risk` na resposta JSON.

## Solução Rápida: Popular Dados de Teste

Se a tabela estiver vazia, você precisa fazer predições primeiro. Use um dos métodos:

### Opção 1: Predição Individual
```bash
curl -X POST http://localhost:10808/predict \
  -H "Content-Type: application/json" \
  -u usuario:senha \
  -d @example-predict-request.json
```

### Opção 2: Predição em Lote
Use o componente "Batch Upload" no frontend para carregar múltiplas predições de uma vez.

## Valores de Planos Configurados

O cálculo usa os seguintes valores mensais:
- Premium: R$ 23,90
- Family: R$ 40,90
- Duo: R$ 31,90
- Student: R$ 12,90
- Free: R$ 0,00

## Próximos Passos

1. Reinicie a aplicação para aplicar as correções
2. Verifique se há dados na tabela `churn_history`
3. Se não houver dados, faça algumas predições
4. Acesse o dashboard e verifique se o card está populado
5. Se ainda estiver vazio, verifique os logs da aplicação

## Logs para Monitorar

Adicione logging temporário no método `calculateRevenueAtRisk()` se necessário:

```java
log.info("TOP 25% by plan: {}", top25ByPlan);
log.info("Revenue at risk calculated: {}", revenueAtRisk);
```


## Como Executar os Testes

### 1. Executar Script SQL de Diagnóstico

Use o arquivo `SQL_TESTE_RECEITA_EM_RISCO.sql` para diagnosticar o problema:

```bash
# Se estiver usando Docker
docker exec -it <container-mysql> mysql -u <usuario> -p <database> < docs/SQL_TESTE_RECEITA_EM_RISCO.sql

# Ou conecte diretamente ao MySQL
mysql -h localhost -u <usuario> -p <database> < docs/SQL_TESTE_RECEITA_EM_RISCO.sql
```

O script irá:
1. Contar total de registros na tabela
2. Mostrar distribuição por tipo de assinatura
3. Calcular TOP 25% por probabilidade
4. Calcular receita em risco manualmente
5. Verificar valores NULL em subscription_type
6. Mostrar distribuição de probabilidades
7. Listar os 10 clientes com maior risco

### 2. Testar o Endpoint da API

```bash
# Substitua usuario:senha pelos valores corretos
curl -u usuario:senha http://localhost:10808/dashboard/metrics | jq .
```

Procure pelo campo `revenue_at_risk` na resposta. Exemplo de resposta esperada:

```json
{
  "total_customers": 1000,
  "customers_at_risk": 250,
  "global_churn_rate": 25.0,
  "revenue_at_risk": 5975.00,
  "model_accuracy": 0.849,
  "risk_factors": [...],
  "feature_importance": [...]
}
```

### 3. Verificar Logs da Aplicação

Se ainda estiver vazio, adicione logging temporário no `DashboardMetricsService.java`:

```java
@Slf4j  // adicione no topo da classe
public class DashboardMetricsService {
    // ...
    
    private double calculateRevenueAtRisk() {
        Map<String, Double> planValues = Map.of(
                "Premium", 23.90,
                "Family", 40.90,
                "Duo", 31.90,
                "Student", 12.90,
                "Free", 0.0
        );

        List<Object[]> top25ByPlan = predictionHistoryQueryPort.getTop25SubscriptionCounts();
        
        log.info("🔍 TOP 25% by plan: {}", top25ByPlan);  // ADICIONE ESTA LINHA
        
        if (top25ByPlan == null || top25ByPlan.isEmpty()) {
            log.warn("⚠️ No data returned from getTop25SubscriptionCounts()");  // ADICIONE ESTA LINHA
            return 0.0;
        }

        double revenue = top25ByPlan.stream()
                .mapToDouble(row -> {
                    String subscriptionType = row[0] == null ? "" : String.valueOf(row[0]);
                    long count = safeLong(row[1]);
                    double planValue = planValues.getOrDefault(subscriptionType, 0.0);
                    double rowRevenue = count * planValue;
                    log.debug("  {} x {} = {}", subscriptionType, count, rowRevenue);  // ADICIONE ESTA LINHA
                    return rowRevenue;
                })
                .sum();
                
        log.info("💰 Total revenue at risk: {}", revenue);  // ADICIONE ESTA LINHA
        return revenue;
    }
}
```

Depois reinicie a aplicação e verifique os logs.
