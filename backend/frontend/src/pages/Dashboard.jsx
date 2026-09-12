import { useState, useMemo, useEffect } from 'react'
import { PredictionForm } from '../components/PredictionForm'
import { BatchUpload } from '../components/BatchUpload'
import { ClientSearch } from '../components/ClientSearch'
import { useClients } from '../hooks/useClients'
import { useData } from '../hooks/useData'
import { motion } from 'framer-motion'
import { getDashboardMetrics } from '../services/api'
import { LayoutDashboard, User, Upload, Search, Activity, AlertTriangle } from 'lucide-react'
import { DashboardSummaryCards } from './dashboard/DashboardSummaryCards'
import { DashboardOverviewTab } from './dashboard/DashboardOverviewTab'
import { riskActionsByFactor } from './dashboard/riskActions'
import { translateFeature } from '../utils/featureLabels'
import { RetentionOperations } from '../components/RetentionOperations'

export default function Dashboard() {
  const { clients, loading: clientsLoading, error: clientsError, refresh: refreshClients } = useClients()
  const { metrics, apiStatus, loading: metricsLoading, refresh: refreshMetrics, fallbackActive } = useData()

  const [selectedClientId, setSelectedClientId] = useState(null)
  const [selectedRiskFactor, setSelectedRiskFactor] = useState("")
  const [activeTab, setActiveTab] = useState('dashboard')

  useEffect(() => {
    refreshClients()
    refreshMetrics()
  }, [])

  // Dados do endpoint /dashboard/metrics
  const [summary, setSummary] = useState(null)
  const [summaryError, setSummaryError] = useState(null)

  // Regra principal: só mostrar dados quando ONLINE
  const canShowData = apiStatus === 'online'

  useEffect(() => {
    if (!canShowData) return

    const load = async () => {
      try {
        const data = await getDashboardMetrics()
        console.log("📊 Resposta da API /dashboard/metrics:", data)
        setSummary(data)
        setSummaryError(null)
      } catch (err) {
        console.error(err)
        setSummaryError("Falha ao carregar dados do dashboard")
        setSummary(null)
      }
    }

    load()
  }, [canShowData])

  // Quando sai do ONLINE, zera tudo que poderia "sobrar" na tela
  useEffect(() => {
    if (!canShowData) {
      setSummary(null)
      setSummaryError(null)
      setSelectedRiskFactor("")
      setSelectedClientId(null)

      // Se estava numa aba que faz request, volta pro dashboard para evitar NetworkError
      if (activeTab !== 'dashboard') setActiveTab('dashboard')
    }
  }, [canShowData]) // eslint-disable-line react-hooks/exhaustive-deps

  const handleBatchSuccess = () => {
    refreshClients()
    refreshMetrics()
  }

  // Principais fatores de risco:
  // - Quando o backend novo existir: usar summary.risk_factors
  // - Enquanto não existir: cai no cálculo local (apenas ONLINE)
  const statsPorMotivo = useMemo(() => {
    if (!canShowData) return {}

    // Se backend já está mandando risk_factors, não usa cálculo local
    if (Array.isArray(summary?.risk_factors) && summary.risk_factors.length > 0) {
      const total = summary.risk_factors.reduce((s, it) => s + (Number(it.count) || 0), 0) || 1
      return summary.risk_factors.reduce((acc, it) => {
        acc[it.name] = { qtd: Number(it.count) || 0, totalRisco: total }
        return acc
      }, {})
    }

    if (!clients || !Array.isArray(clients) || clients.length === 0) return {}

    const getProb = (c) => {
      const val = c.probability ?? c.churnProbability ?? c.churn_probability ?? c.probabilidade
      return val !== undefined ? parseFloat(val) : 0
    }

    const getRiskFactor = (c) => {
      return c.primary_risk_factor || c.primaryRiskFactor || c.main_factor || c.fator_risco || ""
    }

    const emRisco = clients.filter(c => getProb(c) > 0.45)

    return emRisco.reduce((acc, c) => {
      const rawFactor = getRiskFactor(c)
      const factor = translateFeature(rawFactor)
      if (!factor) return acc
      if (!acc[factor]) acc[factor] = { qtd: 0, totalRisco: emRisco.length }
      acc[factor].qtd += 1
      return acc
    }, {})
  }, [clients, canShowData, summary])

  const isInitialLoading = (clientsLoading || metricsLoading) && apiStatus === 'checking'
  const isBackgroundRefreshing = (clientsLoading || metricsLoading) && canShowData

  const hasSummaryData = canShowData && !!summary && (
    Number(summary?.total_customers ?? 0) > 0 ||
    (Array.isArray(summary?.risk_factors) && summary.risk_factors.length > 0)
  )

  if (isInitialLoading) {
    return (
      <div style={{ background: '#121212', color: 'white', height: '100vh', padding: '50px', textAlign: 'center' }}>
        <div style={{ fontSize: '1.5rem', marginBottom: '20px' }}>🔄 Sincronizando Inteligência...</div>
        <div style={{ color: '#b3b3b3' }}>Conectando à API ChurnInsight</div>
      </div>
    )
  }

  if (clientsError) {
    return (
      <div style={{ background: '#121212', color: '#ff4d4d', height: '100vh', padding: '50px', textAlign: 'center' }}>
        <div style={{ fontSize: '1.5rem', marginBottom: '20px' }}>⚠️ Erro ao carregar dados</div>
        <p style={{ color: '#b3b3b3' }}>{clientsError}</p>
        <button
          onClick={() => window.location.reload()}
          style={{ marginTop: '20px', padding: '10px 20px', background: '#1DB954', border: 'none', borderRadius: '4px', cursor: 'pointer', fontWeight: 'bold' }}
        >
          Tentar Novamente
        </button>
      </div>
    )
  }

  // Cards: quando OFFLINE => "—"
    // Fallback warning
    const fallbackWarning = fallbackActive ? (
      <div style={{ background: '#242424', color: '#f59e0b', padding: '16px', borderRadius: '8px', marginBottom: '18px', textAlign: 'center', fontWeight: 'bold', fontSize: '1.1rem' }}>
        ⚠️ Dados carregados do fallback local.<br />
        As métricas podem estar desatualizadas ou incompletas.<br />
        Verifique a conexão com a API para dados em tempo real.
      </div>
    ) : null
  const safeTotalCustomers = canShowData
    ? (hasSummaryData ? (summary?.total_customers ?? '—') : (metrics?.totalClients ?? '—'))
    : '—'

  const safeMonitoringRate = canShowData
    ? (((hasSummaryData ? summary?.global_churn_rate : null) ?? (metrics?.globalChurnRate !== undefined ? Number(metrics.globalChurnRate) : null)) ?? null)
    : null
  const safeMonitoringLabel = safeMonitoringRate === null ? '—' : `${Number(safeMonitoringRate).toFixed(1)}%`

  const safeCustomersAtRisk = canShowData
    ? ((hasSummaryData ? summary?.customers_at_risk : null) ?? metrics?.highRiskCount ?? '—')
    : '—'

  const safeRevenueAtRisk = canShowData
    ? (((hasSummaryData ? summary?.revenue_at_risk : null) ?? metrics?.revenueAtRisk ?? null) ?? null)
    : null
  const safeRevenueLabel =
    safeRevenueAtRisk === null
      ? '—'
      : new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(safeRevenueAtRisk)

  const safeModelAccuracy = canShowData
    ? (((hasSummaryData ? summary?.model_accuracy : null) ?? null) ?? (metrics?.modelAccuracy !== undefined ? Number(metrics.modelAccuracy) : null))
    : null
  const safeAccuracyLabel =
    safeModelAccuracy === null
      ? '—'
      : (safeModelAccuracy > 1
        ? `${Number(safeModelAccuracy).toFixed(1)}%`
        : `${(Number(safeModelAccuracy) * 100).toFixed(1)}%`)

  const apiStatusBadge = {
    online: { color: '#1DB954', text: '🟢 API Online' },
    offline: { color: '#ff4d4d', text: '🔴 API Offline' },
    degraded: { color: '#ffcc00', text: '🟡 API Degradada' },
    checking: { color: '#b3b3b3', text: '⏳ Verificando...' },
  }[apiStatus] || { color: '#b3b3b3', text: '❓ Desconhecido' }

  const selectedClient = (canShowData && clients && clients.length > 0)
    ? clients.find(c => String(c.clientId) === String(selectedClientId)) || clients[0]
    : null

  const motivoInfo = riskActionsByFactor[selectedRiskFactor]
  const motivoStats = statsPorMotivo[selectedRiskFactor]

  const tabStyle = (isActive, disabled = false) => ({
    padding: '12px 24px',
    background: isActive ? '#1DB954' : '#242424',
    color: isActive ? '#000' : '#fff',
    border: 'none',
    cursor: disabled ? 'not-allowed' : 'pointer',
    opacity: disabled ? 0.6 : 1,
    borderRadius: '24px',
    fontWeight: 'bold',
    transition: 'all 0.2s',
    display: 'flex',
    alignItems: 'center',
    gap: '8px'
  })

  const OfflineBlock = ({ text }) => (
    <div style={{
        height: '350px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#666',
        fontStyle: 'italic',
        whiteSpace: 'pre-line',
        textAlign: 'center'
    }}>
        {text}
    </div>
)

  const OfflineScreen = ({ title }) => (
    <div style={{ background: '#242424', padding: '30px', borderRadius: '8px' }}>
      <h3 style={{ marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '10px' }}>
        <AlertTriangle size={20} color="#ffcc00" />
        {title}
      </h3>
      <p style={{ color: '#b3b3b3', margin: 0 }}>
        API indisponível: este recurso só funciona quando o status for <b>ONLINE</b>.
      </p>
    </div>
  )

  const totalBase = canShowData
    ? Number((hasSummaryData ? summary?.total_customers : null) ?? metrics?.totalClients ?? 0)
    : 0

  const customersForAction = canShowData
    ? Number((hasSummaryData ? summary?.customers_at_risk : null) ?? metrics?.highRiskCount ?? 0)
    : 0

  const actionBaseDistribution = totalBase > 0
    ? [Math.max(totalBase - customersForAction, 0), customersForAction]
    : null

  const featImportance = hasSummaryData
    ? (Array.isArray(summary?.feature_importance) && summary.feature_importance.length > 0
      ? summary.feature_importance
      : null)
    : (Array.isArray(metrics?.featureImportance) && metrics.featureImportance.length > 0
      ? metrics.featureImportance
      : null)

  return (
    <div style={{ padding: '40px', maxWidth: '1400px', margin: '0 auto', fontFamily: 'Circular, sans-serif' }}>
      {fallbackWarning}
      {/* HEADER */}
      <motion.div
        initial={{ opacity: 0, y: -20 }}
        animate={{ opacity: 1, y: 0 }}
        style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '18px' }}
      >
        <div>
          <h1 style={{ fontSize: '2.5rem', fontWeight: 900, marginBottom: '10px' }}>
            <span style={{ color: '#1DB954' }}>Churn</span>Insight
          </h1>
          <p style={{ color: '#b3b3b3' }}>Dashboard de Retenção de Clientes (Spotify Edition)</p>
        </div>
        <div style={{ textAlign: 'right' }}>
          <div style={{ padding: '8px 16px', borderRadius: '20px', background: '#242424', display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
            <Activity size={16} color={apiStatusBadge.color} />
            <span style={{ color: apiStatusBadge.color, fontWeight: 'bold' }}>{apiStatusBadge.text}</span>
          </div>
          {isBackgroundRefreshing ? (
            <p style={{ marginTop: '8px', fontSize: '0.8rem', color: '#1DB954', fontWeight: 'bold' }}>
              🔄 Atualizando dados...
            </p>
          ) : (
            <p style={{ marginTop: '8px', fontSize: '0.8rem', color: '#535353' }}>
              Última sincronização: {new Date().toLocaleTimeString()}
            </p>
          )}
        </div>
      </motion.div>

      {/* AVISO QUANDO NÃO ONLINE */}
      {!canShowData && (
        <div style={{
          background: '#242424',
          border: '1px solid #333',
          padding: '14px 16px',
          borderRadius: '8px',
          marginBottom: '22px',
          color: '#ffcc00',
          fontWeight: 'bold'
        }}>
          ⚠️ API indisponível: nenhum dado será exibido enquanto o status não for ONLINE.
        </div>
      )}

      {/* Mensagens do summary só quando online */}
      {canShowData && !summary && !summaryError && <p style={{ color: '#b3b3b3' }}>Carregando dados...</p>}
      {canShowData && summaryError && <p style={{ color: '#ff4d4d' }}>{summaryError}</p>}

      <DashboardSummaryCards
        canShowData={canShowData}
        totalCustomers={safeTotalCustomers}
        monitoringLabel={safeMonitoringLabel}
        customersAtRisk={safeCustomersAtRisk}
        revenueLabel={safeRevenueLabel}
        accuracyLabel={safeAccuracyLabel}
      />

      {/* TABS */}
      <div style={{ marginBottom: '30px', borderBottom: '1px solid #333', paddingBottom: '15px', display: 'flex', gap: '20px', flexWrap: 'wrap' }}>
        <button onClick={() => setActiveTab('dashboard')} style={tabStyle(activeTab === 'dashboard')}>
          <LayoutDashboard size={18} /> Dashboard
        </button>

        {/* desabilita as abas que fazem request quando offline */}
        <button
          onClick={() => canShowData && setActiveTab('prediction')}
          style={tabStyle(activeTab === 'prediction', !canShowData)}
          disabled={!canShowData}
          title={!canShowData ? "Disponível apenas com API ONLINE" : ""}
        >
          <User size={18} /> Predição Individual
        </button>

        <button
          onClick={() => canShowData && setActiveTab('batch')}
          style={tabStyle(activeTab === 'batch', !canShowData)}
          disabled={!canShowData}
          title={!canShowData ? "Disponível apenas com API ONLINE" : ""}
        >
          <Upload size={18} /> Upload em Lote
        </button>

        <button
          onClick={() => canShowData && setActiveTab('search')}
          style={tabStyle(activeTab === 'search', !canShowData)}
          disabled={!canShowData}
          title={!canShowData ? "Disponível apenas com API ONLINE" : ""}
        >
          <Search size={18} /> Buscar Cliente
        </button>

        <button
          onClick={() => canShowData && setActiveTab('retention')}
          style={tabStyle(activeTab === 'retention', !canShowData)}
          disabled={!canShowData}
          title={!canShowData ? "Disponível apenas com API ONLINE" : ""}
        >
          <Activity size={18} /> Operações de Retenção
        </button>
      </div>

      {activeTab === 'dashboard' && (
        <DashboardOverviewTab
          canShowData={canShowData}
          actionBaseDistribution={actionBaseDistribution}
          featImportance={featImportance}
          selectedClient={selectedClient}
          selectedClientId={selectedClientId}
          statsPorMotivo={statsPorMotivo}
          selectedRiskFactor={selectedRiskFactor}
          setSelectedRiskFactor={setSelectedRiskFactor}
          motivoInfo={motivoInfo}
          motivoStats={motivoStats}
          OfflineBlock={OfflineBlock}
        />
      )}

      {/* OUTRAS ABAS */}
      {activeTab === 'prediction' && (canShowData ? (
        <motion.div key="prediction" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }} style={{ maxWidth: '800px', margin: '0 auto' }}>
          <div style={{ background: '#242424', padding: '40px', borderRadius: '8px' }}>
            <h2 style={{ marginBottom: '30px', borderBottom: '1px solid #333', paddingBottom: '15px' }}>Simulador de Churn</h2>
            <PredictionForm />
          </div>
        </motion.div>
      ) : (
        <OfflineScreen title="Predição Individual" />
      ))}

      {activeTab === 'batch' && (canShowData ? (
        <motion.div key="batch" initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.3 }}>
          <BatchUpload onUploadSuccess={handleBatchSuccess} />
        </motion.div>
      ) : (
        <OfflineScreen title="Upload em Lote" />
      ))}

      {activeTab === 'search' && (canShowData ? (
        <motion.div key="search" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
          <ClientSearch />
        </motion.div>
      ) : (
        <OfflineScreen title="Buscar Cliente" />
      ))}

      {activeTab === 'retention' && (canShowData ? (
        <motion.div key="retention" initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.3 }}>
          <RetentionOperations />
        </motion.div>
      ) : (
        <OfflineScreen title="Operações de Retenção" />
      ))}

    </div>
  )
}