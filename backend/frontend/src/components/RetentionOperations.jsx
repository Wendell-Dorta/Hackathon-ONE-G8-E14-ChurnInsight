import { useEffect, useMemo, useState } from 'react'
import {
  getPrioritizedRetention,
  getRetentionKpis,
  createRetentionAction,
  updateRetentionActionStatus,
} from '../services/api'

const boxStyle = {
  background: '#242424',
  padding: '24px',
  borderRadius: '8px',
}

const fmtCurrency = (value) => new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(Number(value || 0))

export function RetentionOperations() {
  const [kpis, setKpis] = useState(null)
  const [prioritized, setPrioritized] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [submittingId, setSubmittingId] = useState(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const [kpisData, prioritizedData] = await Promise.all([
        getRetentionKpis(),
        getPrioritizedRetention(0, 15),
      ])
      setKpis(kpisData)
      setPrioritized(Array.isArray(prioritizedData?.content) ? prioritizedData.content : [])
    } catch (err) {
      setError(err?.message || 'Falha ao carregar operações de retenção')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const sortedPrioritized = useMemo(() => {
    return [...prioritized].sort((a, b) => Number(b.priority_score || 0) - Number(a.priority_score || 0))
  }, [prioritized])

  const topRevenue = useMemo(() => sortedPrioritized.reduce((sum, item) => sum + Number(item.expected_monthly_value || 0), 0), [sortedPrioritized])

  const highThreshold = Number(kpis?.priority_high_threshold ?? 6)
  const mediumThreshold = Number(kpis?.priority_medium_threshold ?? 3)

  const getPriorityBadge = (score) => {
    const value = Number(score || 0)
    if (value >= highThreshold) return { label: 'ALTA', bg: '#7f1d1d', color: '#fecaca', border: '#ef4444' }
    if (value >= mediumThreshold) return { label: 'MEDIA', bg: '#78350f', color: '#fde68a', border: '#f59e0b' }
    return { label: 'BAIXA', bg: '#1e3a8a', color: '#bfdbfe', border: '#60a5fa' }
  }

  const handleCreateAction = async (item) => {
    try {
      setSubmittingId(item.client_id)
      await createRetentionAction({
        clientId: item.client_id,
        actionType: item.suggested_action,
        channel: 'EMAIL',
        owner: 'Time de Retencao',
        notes: `Acao sugerida automaticamente para score ${item.priority_score}`,
      })
      await load()
    } catch (err) {
      setError(err?.message || 'Nao foi possivel criar a acao')
    } finally {
      setSubmittingId(null)
    }
  }

  const handleMarkExecuted = async (item) => {
    try {
      setSubmittingId(item.client_id)
      await updateRetentionActionStatus(item.action_id, 'EXECUTED')
      await load()
    } catch (err) {
      setError(err?.message || 'Nao foi possivel atualizar status da acao')
    } finally {
      setSubmittingId(null)
    }
  }

  if (loading) {
    return <div style={boxStyle}>Carregando operação de retenção...</div>
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
      {error && (
        <div style={{ ...boxStyle, borderLeft: '4px solid #ef4444', color: '#fecaca' }}>
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '14px' }}>
        <div style={{ ...boxStyle, borderLeft: '4px solid #38bdf8' }}>
          <p style={{ margin: 0, color: '#94a3b8', fontSize: '0.8rem' }}>Ações criadas (30 dias)</p>
          <h3 style={{ margin: '8px 0 0 0' }}>{kpis?.total_actions ?? 0}</h3>
        </div>
        <div style={{ ...boxStyle, borderLeft: '4px solid #f59e0b' }}>
          <p style={{ margin: 0, color: '#fde68a', fontSize: '0.8rem' }}>Taxa de execução</p>
          <h3 style={{ margin: '8px 0 0 0' }}>{Number(kpis?.execution_rate ?? 0).toFixed(1)}%</h3>
        </div>
        <div style={{ ...boxStyle, borderLeft: '4px solid #22c55e' }}>
          <p style={{ margin: 0, color: '#bbf7d0', fontSize: '0.8rem' }}>Taxa de retenção</p>
          <h3 style={{ margin: '8px 0 0 0' }}>{Number(kpis?.retention_rate ?? 0).toFixed(1)}%</h3>
        </div>
        <div style={{ ...boxStyle, borderLeft: '4px solid #fb923c' }}>
          <p style={{ margin: 0, color: '#fed7aa', fontSize: '0.8rem' }}>Receita recuperada</p>
          <h3 style={{ margin: '8px 0 0 0' }}>{fmtCurrency(kpis?.recovered_revenue)}</h3>
        </div>
      </div>

      <div style={{ ...boxStyle }}>
        <h3 style={{ marginTop: 0, marginBottom: '8px' }}>Fila Priorizada de Retenção (TOP 25%)</h3>
        <p style={{ marginTop: 0, marginBottom: '16px', color: '#9ca3af', fontSize: '0.85rem' }}>
          Receita mensal potencial no recorte atual: <strong>{fmtCurrency(topRevenue)}</strong>
        </p>

        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '920px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid #3a3a3a', textAlign: 'left' }}>
                <th style={{ padding: '10px 8px' }}>Cliente</th>
                <th style={{ padding: '10px 8px' }}>Prob.</th>
                <th style={{ padding: '10px 8px' }}>Plano</th>
                <th style={{ padding: '10px 8px' }}>Valor mensal</th>
                <th style={{ padding: '10px 8px' }}>Sinal de risco</th>
                <th style={{ padding: '10px 8px' }}>Score</th>
                <th style={{ padding: '10px 8px' }}>Prioridade</th>
                <th style={{ padding: '10px 8px' }}>Ação sugerida</th>
                <th style={{ padding: '10px 8px' }}>Status ação</th>
                <th style={{ padding: '10px 8px' }}>Operação</th>
              </tr>
            </thead>
            <tbody>
              {sortedPrioritized.map((item) => {
                const badge = getPriorityBadge(item.priority_score)
                return (
                <tr key={item.client_id} style={{ borderBottom: '1px solid #2f2f2f' }}>
                  <td style={{ padding: '10px 8px' }}>{item.user_id || item.client_id}</td>
                  <td style={{ padding: '10px 8px' }}>{(Number(item.probability) * 100).toFixed(1)}%</td>
                  <td style={{ padding: '10px 8px' }}>{item.subscription_type}</td>
                  <td style={{ padding: '10px 8px' }}>{fmtCurrency(item.expected_monthly_value)}</td>
                  <td style={{ padding: '10px 8px' }}>{Number(item.risk_signal || 0).toFixed(4)}</td>
                  <td style={{ padding: '10px 8px' }}>{Number(item.priority_score).toFixed(4)}</td>
                  <td style={{ padding: '10px 8px' }}>
                    <span style={{
                      display: 'inline-block',
                      padding: '2px 8px',
                      borderRadius: '999px',
                      border: `1px solid ${badge.border}`,
                      background: badge.bg,
                      color: badge.color,
                      fontSize: '0.72rem',
                      fontWeight: 800,
                    }}>
                      {badge.label}
                    </span>
                  </td>
                  <td style={{ padding: '10px 8px' }}>{item.suggested_action}</td>
                  <td style={{ padding: '10px 8px' }}>{item.action_status || 'SEM ACAO'}</td>
                  <td style={{ padding: '10px 8px' }}>
                    {!item.action_id ? (
                      <button
                        onClick={() => handleCreateAction(item)}
                        disabled={submittingId === item.client_id}
                        style={{
                          padding: '6px 10px',
                          border: 'none',
                          borderRadius: '4px',
                          fontWeight: 700,
                          cursor: 'pointer',
                          background: '#22c55e',
                          color: '#041105',
                          marginRight: '8px',
                        }}
                      >
                        Criar ação
                      </button>
                    ) : item.action_status !== 'EXECUTED' ? (
                      <button
                        onClick={() => handleMarkExecuted(item)}
                        disabled={submittingId === item.client_id}
                        style={{
                          padding: '6px 10px',
                          border: '1px solid #f59e0b',
                          borderRadius: '4px',
                          fontWeight: 700,
                          cursor: 'pointer',
                          background: '#1f1f1f',
                          color: '#f59e0b',
                        }}
                      >
                        Executada
                      </button>
                    ) : (
                      <span style={{ color: '#22c55e', fontWeight: 700 }}>Concluída</span>
                    )}
                  </td>
                </tr>
              )})}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
