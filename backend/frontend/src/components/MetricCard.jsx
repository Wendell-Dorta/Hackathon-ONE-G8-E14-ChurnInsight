import { motion } from 'framer-motion';

const toneMap = {
  neutral: {
    border: '#334155',
    background: 'linear-gradient(180deg, #1f2937 0%, #111827 100%)',
    label: '#cbd5e1',
    value: '#f8fafc',
    glow: 'rgba(148, 163, 184, 0.20)',
  },
  warning: {
    border: '#f59e0b',
    background: 'linear-gradient(180deg, #3b2a08 0%, #1f1605 100%)',
    label: '#fde68a',
    value: '#fef3c7',
    glow: 'rgba(245, 158, 11, 0.18)',
  },
  danger: {
    border: '#ef4444',
    background: 'linear-gradient(180deg, #3a1111 0%, #1f0909 100%)',
    label: '#fecaca',
    value: '#fee2e2',
    glow: 'rgba(239, 68, 68, 0.18)',
  },
  revenue: {
    border: '#f97316',
    background: 'linear-gradient(180deg, #3a1b0c 0%, #1f1007 100%)',
    label: '#fdba74',
    value: '#ffedd5',
    glow: 'rgba(249, 115, 22, 0.18)',
  },
  info: {
    border: '#38bdf8',
    background: 'linear-gradient(180deg, #0c2533 0%, #08161f 100%)',
    label: '#bae6fd',
    value: '#e0f2fe',
    glow: 'rgba(56, 189, 248, 0.18)',
  },
}

export function MetricCard({ title, value, subtitle, tone = 'neutral' }) {
  const palette = toneMap[tone] || toneMap.neutral

  return (
    <motion.div
      className="card"
      style={{
        minWidth: 146,
        background: palette.background,
        border: `1px solid ${palette.border}`,
        boxShadow: `inset 0 1px 0 ${palette.glow}`,
        textAlign: 'center',
        display: 'grid',
        gridTemplateRows: '5px 50px 52px 54px',
        justifyItems: 'center',
        alignItems: 'center',
        minHeight: 166,
        padding: '0.95rem 0.82rem',
      }}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      whileHover={{ scale: 1.05 }}
    >
      <div
        style={{
          width: 34,
          height: 2,
          borderRadius: 999,
          background: palette.border,
          marginInline: 'auto',
        }}
      />
      <p style={{ color: palette.label, margin: 0, fontSize: '0.76rem', fontWeight: 600, lineHeight: 1.2, maxWidth: 154, textAlign: 'center', textWrap: 'balance', marginInline: 'auto' }}>
        {title}
      </p>
      <p style={{ color: palette.value, fontSize: 'clamp(1.42rem, 1.95vw, 1.82rem)', fontWeight: 800, lineHeight: 1, margin: 0, textAlign: 'center', marginInline: 'auto', fontVariantNumeric: 'tabular-nums' }}>
        {value}
      </p>
      <p
        style={{
          color: palette.label,
          opacity: subtitle ? 0.82 : 0,
          fontSize: '0.69rem',
          margin: 0,
          lineHeight: 1.32,
          maxWidth: 156,
          textAlign: 'center',
          textWrap: 'balance',
          marginInline: 'auto',
        }}
      >
        {subtitle || 'placeholder'}
      </p>
    </motion.div>
  )
}
