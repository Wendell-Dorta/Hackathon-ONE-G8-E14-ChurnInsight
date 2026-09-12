# 📊 Correção: Card "Receita Potencial em Risco"

## 📁 Documentação Disponível

Este diretório contém toda a documentação relacionada à correção do problema do card "Receita Potencial em Risco" vazio.

### Arquivos Criados

1. **SOLUCAO_RAPIDA.md** ⚡
   - Guia rápido para aplicar as correções
   - Comandos prontos para copiar e colar
   - Verificações essenciais
   - **Comece por aqui!**

2. **DIAGNOSTICO_RECEITA_EM_RISCO.md** 🔍
   - Análise detalhada do problema
   - Fluxo completo de dados
   - Instruções de debug
   - Como adicionar logs

3. **RESUMO_CORRECOES_RECEITA.md** 📋
   - Lista de todas as correções aplicadas
   - Arquivos modificados
   - Próximos passos
   - Valores de planos configurados

4. **SQL_TESTE_RECEITA_EM_RISCO.sql** 🗄️
   - 8 queries de diagnóstico
   - Testa a lógica do TOP 25%
   - Calcula receita manualmente
   - Verifica integridade dos dados

5. **DADOS_TESTE_RECEITA.sql** 🧪
   - Script para popular dados de teste
   - 100 clientes com diferentes perfis
   - TOP 25% com alta probabilidade
   - Distribuição realista de planos

## 🚀 Início Rápido

### 1. Aplicar Correções

```bash
# Reiniciar com Docker
docker-compose down
docker-compose up --build -d
```

### 2. Popular Dados de Teste (se necessário)

```bash
# Executar script de dados de teste
docker exec -it <container-mysql> mysql -u root -p churninsight < docs/DADOS_TESTE_RECEITA.sql
```

### 3. Verificar Resultado

```bash
# Testar endpoint
curl -u usuario:senha http://localhost:10808/dashboard/metrics | jq .revenue_at_risk

# Acessar dashboard
# http://localhost:5173
```

## 🔧 Correções Aplicadas

### Backend

1. **PredictionHistoryRepository.java**
   - Query SQL otimizada com `ORDER BY`
   - Alias explícito para melhor legibilidade

2. **DashboardMetricsService.java**
   - Proteção contra lista vazia
   - Retorna 0.0 quando não há dados

### Frontend

3. **useData.js**
   - Removido `return` duplicado
   - Corrigido retorno de `fallbackActive`

## 📊 Como Funciona

### Cálculo da Receita em Risco

```
1. Seleciona TOP 25% clientes por probabilidade de churn
2. Agrupa por tipo de assinatura
3. Multiplica quantidade × valor do plano
4. Soma total
```

### Valores dos Planos

| Plano    | Valor/Mês |
|----------|-----------|
| Premium  | R$ 23,90  |
| Family   | R$ 40,90  |
| Duo      | R$ 31,90  |
| Student  | R$ 12,90  |
| Free     | R$ 0,00   |

### Exemplo de Cálculo

Se o TOP 25% tiver:
- 10 Premium = 10 × R$ 23,90 = R$ 239,00
- 8 Family = 8 × R$ 40,90 = R$ 327,20
- 5 Duo = 5 × R$ 31,90 = R$ 159,50
- 2 Student = 2 × R$ 12,90 = R$ 25,80

**Total em Risco**: R$ 751,50

## 🐛 Troubleshooting

### Card ainda está vazio?

1. **Verificar se há dados**
   ```sql
   SELECT COUNT(*) FROM churn_history;
   ```

2. **Executar diagnóstico**
   ```bash
   docker exec -it <container-mysql> mysql -u root -p churninsight < docs/SQL_TESTE_RECEITA_EM_RISCO.sql
   ```

3. **Verificar logs**
   ```bash
   docker-compose logs -f backend | grep -i revenue
   ```

4. **Verificar API**
   ```bash
   curl -u usuario:senha http://localhost:10808/dashboard/metrics
   ```

### Erros Comuns

| Erro | Causa | Solução |
|------|-------|---------|
| Card mostra "—" | API offline | Verificar status da API |
| Valor R$ 0,00 | Só clientes Free | Normal, Free não gera receita |
| Erro 401 | Credenciais erradas | Verificar usuário/senha |
| Erro 500 | Erro no backend | Verificar logs do container |

## 📚 Estrutura de Arquivos

```
docs/
├── README_CORRECAO_RECEITA.md          # Este arquivo
├── SOLUCAO_RAPIDA.md                   # Guia rápido
├── DIAGNOSTICO_RECEITA_EM_RISCO.md     # Diagnóstico detalhado
├── RESUMO_CORRECOES_RECEITA.md         # Resumo das correções
├── SQL_TESTE_RECEITA_EM_RISCO.sql      # Queries de teste
└── DADOS_TESTE_RECEITA.sql             # Dados de exemplo
```

## 🎯 Fluxo de Dados

```
┌─────────────────┐
│   Dashboard     │
│   (Frontend)    │
└────────┬────────┘
         │ GET /dashboard/metrics
         ▼
┌─────────────────┐
│ Dashboard       │
│ Controller      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Dashboard       │
│ Metrics Service │
└────────┬────────┘
         │ calculateRevenueAtRisk()
         ▼
┌─────────────────┐
│ Prediction      │
│ History Repo    │
└────────┬────────┘
         │ getTop25SubscriptionCounts()
         ▼
┌─────────────────┐
│   MySQL         │
│ churn_history   │
└─────────────────┘
```

## ✅ Checklist de Verificação

- [ ] Correções aplicadas no código
- [ ] Aplicação reiniciada
- [ ] Banco de dados tem dados
- [ ] Endpoint retorna `revenue_at_risk`
- [ ] Card mostra valor no dashboard
- [ ] Logs não mostram erros

## 📞 Suporte

Se o problema persistir:

1. Execute o diagnóstico completo
2. Verifique os logs da aplicação
3. Teste o endpoint diretamente
4. Verifique o console do navegador (F12)
5. Consulte a documentação detalhada

## 🔗 Links Úteis

- [Documentação Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [MySQL Window Functions](https://dev.mysql.com/doc/refman/8.0/en/window-functions.html)
- [React Hooks](https://react.dev/reference/react)

---

**Versão**: 1.0  
**Data**: 2024  
**Autor**: Equipe ChurnInsight
