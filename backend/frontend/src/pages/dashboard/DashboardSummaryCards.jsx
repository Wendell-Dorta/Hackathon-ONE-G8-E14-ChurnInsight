import { MetricCard } from '../../components/MetricCard'

export function DashboardSummaryCards({
  canShowData,
  totalCustomers,
  monitoringLabel,
  customersAtRisk,
  revenueLabel,
  accuracyLabel,
}) {
  return (
    <>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
          gap: '16px',
          marginBottom: '20px',
          alignItems: 'stretch',
        }}
      >
        <MetricCard title="Total de Clientes" value={totalCustomers} tone="neutral" />
        <MetricCard
          title="Clientes Prioritários para Ação"
          value={monitoringLabel}
          subtitle="Top 25% da base com maior instabilidade"
          tone="warning"
        />
        <MetricCard
          title="Clientes em Risco"
          value={customersAtRisk}
          subtitle="Quantidade em observação imediata"
          tone="danger"
        />
        <MetricCard
          title="Receita Potencial em Risco (Est.)"
          value={revenueLabel}
          subtitle="Estimativa financeira do TOP 25% em risco"
          tone="revenue"
        />
        <MetricCard
          title="Precisão do Modelo"
          value={accuracyLabel}
          subtitle="Indicador técnico do modelo atual"
          tone="info"
        />
      </div>

      {canShowData && (
        <div
          style={{
            fontSize: '0.85rem',
            color: '#888',
            marginBottom: '40px',
            padding: '12px',
            background: '#1a1a1a',
            borderRadius: '6px',
            borderLeft: '3px solid #f59e0b',
          }}
        >
          <strong>Nota:</strong> O gráfico principal segmenta a base entre clientes estáveis e clientes que exigem ação imediata. Assim, a leitura visual fica alinhada com o critério operacional usado no restante do dashboard.
        </div>
      )}
    </>
  )
}
