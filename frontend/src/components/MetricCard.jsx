import { motion } from 'framer-motion';

export function MetricCard({ title, value }) {
  return (
    <motion.div
      className="card"
      style={{ minWidth: 160 }}
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      whileHover={{ scale: 1.05 }}
    >
      <p style={{ color: '#b3b3b3' }}>{title}</p>
      <p className="metric-value">{value}</p>
    </motion.div>
  )
}
