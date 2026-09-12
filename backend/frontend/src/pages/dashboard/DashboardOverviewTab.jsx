import { motion } from 'framer-motion'
import { Activity, AlertTriangle, User } from 'lucide-react'
import { ChurnDistributionChart, FeatureImportanceChart } from '../../components/Charts'
import { ClientExplainability } from '../../components/ClientExplainability'

export function DashboardOverviewTab({
  canShowData,
  actionBaseDistribution,
  featImportance,
  selectedClient,
  selectedClientId,
  statsPorMotivo,
  selectedRiskFactor,
  setSelectedRiskFactor,
  motivoInfo,
  motivoStats,
  OfflineBlock,
}) {
  return (
    <motion.div
      key="dashboard"
      initial={{ opacity: 0, x: -10 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3 }}
      style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '30px' }}
    >
      <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
        <div style={{ background: '#242424', padding: '30px', borderRadius: '8px' }}>
          <h3 style={{ marginBottom: '25px', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <AlertTriangle size={20} color="#f59e0b" />
            Segmentação Operacional da Base
          </h3>

          {!canShowData ? (
            <OfflineBlock text="Dados indisponíveis (API Offline)" />
          ) : !actionBaseDistribution ? (
            <OfflineBlock text="Sem dados de segmentação disponíveis no momento." />
          ) : (
            <div style={{ height: '350px' }}>
              <ChurnDistributionChart
                data={actionBaseDistribution}
                labels={['Base estável', 'Prioridade de ação']}
                colors={['#475569', '#f59e0b']}
                hoverColors={['#64748b', '#fbbf24']}
              />
            </div>
          )}
        </div>

        <div style={{ background: '#242424', padding: '30px', borderRadius: '8px' }}>
          <h3 style={{ marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <Activity size={20} color="#1DB954" />
            Importância das Variáveis (Top 10)
          </h3>
          <p style={{ fontSize: '0.85rem', color: '#888', marginBottom: '20px' }}>
            Variáveis com maior peso na predição do modelo
          </p>

          {!canShowData ? (
            <OfflineBlock text="Dados indisponíveis (API Offline)" />
          ) : !featImportance ? (
            <OfflineBlock text="A versão atual do modelo utiliza Regressão Logística com SMOTE, que não fornece interpretabilidade nativa de features. Esta funcionalidade requer algoritmos com suporte a feature importance (ex: XGBoost, Random Forest)." />
          ) : (
            <div style={{ height: '350px' }}>
              <FeatureImportanceChart data={featImportance} />
            </div>
          )}
        </div>

        {canShowData && selectedClient && (
          <div style={{ background: '#242424', padding: '30px', borderRadius: '8px' }}>
            <h3 style={{ marginBottom: '25px', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <User size={20} color="#1DB954" />
              {selectedClientId ? 'Análise Detalhada do Cliente Selecionado' : 'Análise Detalhada (Amostra)'}
            </h3>
            <ClientExplainability client={selectedClient} />
          </div>
        )}
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '30px' }}>
        <div style={{ background: '#242424', padding: '30px', borderRadius: '8px' }}>
          <h3 style={{ marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '10px' }}>
            <AlertTriangle size={20} color="#f59e0b" />
            Principais Fatores de Risco
          </h3>
          <p style={{ fontSize: '0.85rem', color: '#888', marginBottom: '15px' }}>
            Fatores mais frequentes entre os clientes prioritários
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {!canShowData ? (
              <p style={{ color: '#666', fontStyle: 'italic' }}>Dados indisponíveis (API Offline)</p>
            ) : Object.keys(statsPorMotivo).length === 0 ? (
              <p style={{ color: '#666', fontStyle: 'italic' }}>Nenhum fator de risco detectado nos dados carregados.</p>
            ) : (
              Object.entries(statsPorMotivo)
                .sort(([, a], [, b]) => b.qtd - a.qtd)
                .map(([motivo, { qtd, totalRisco }]) => (
                  <motion.div
                    key={motivo}
                    whileHover={{ scale: 1.02, backgroundColor: '#2a2a2a' }}
                    onClick={() => setSelectedRiskFactor(motivo === selectedRiskFactor ? '' : motivo)}
                    style={{
                      padding: '15px',
                      borderRadius: '8px',
                      background: '#181818',
                      cursor: 'pointer',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      border: motivo === selectedRiskFactor ? '1px solid #f59e0b' : 'none',
                    }}
                  >
                    <div style={{ flex: 1 }}>
                      <p style={{ margin: 0, color: '#b3b3b3', fontSize: '0.9rem' }}>{motivo}</p>
                      <h4 style={{ margin: 0, color: '#fff', fontSize: '1.2rem' }}>{qtd} usuários</h4>
                    </div>
                    <div style={{ color: '#f59e0b', fontSize: '1.5rem' }}>
                      {totalRisco > 0 ? ((qtd / totalRisco) * 100).toFixed(1) : '0.0'}%
                    </div>
                  </motion.div>
                ))
            )}
          </div>
        </div>

        {canShowData && selectedRiskFactor && motivoInfo && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            style={{ background: '#2a2a2a', padding: '20px', borderRadius: '8px', borderLeft: '4px solid #ffcc00' }}
          >
            <h4 style={{ color: '#ffcc00', margin: '0 0 10px 0' }}>{motivoInfo.nome}</h4>
            <p style={{ color: '#b3b3b3', fontSize: '0.9rem', marginBottom: '15px' }}>
              {motivoStats?.qtd} clientes impactados
            </p>
            <p style={{ color: '#fff', fontSize: '0.95rem', fontStyle: 'italic' }}>
              "Ação Recomendada: {motivoInfo.acao}"
            </p>
          </motion.div>
        )}
      </div>
    </motion.div>
  )
}
