export const FEATURE_LABELS = {
  gender: 'Gênero',
  gender_Male: 'Gênero Masculino',
  gender_Female: 'Gênero Feminino',
  age: 'Idade',
  Age: 'Idade',
  country: 'País',
  country_FR: 'País França',
  country_IN: 'País Índia',
  subscription_type: 'Tipo de Assinatura',
  subscription_type_Student: 'Assinatura Estudante',
  listening_time: 'Tempo de Escuta',
  songs_played_per_day: 'Músicas por Dia',
  skip_rate: 'Taxa de Pulagem',
  device_type: 'Tipo de Dispositivo',
  ads_listened_per_week: 'Anúncios por Semana',
  offline_listening: 'Uso Offline',
  is_churned: 'Cancelamento (Churn)',
  songs_per_minute: 'Músicas por Minuto',
  ad_intensity: 'Intensidade de Anúncios',
  frustration_index: 'Índice de Frustração',
  is_heavy_user: 'Usuário Intenso (Heavy)',
  premium_no_offline: 'Premium sem Offline',
  premium_sub_month: 'Meses de Assinatura Premium',
  fav_genre: 'Gênero Favorito',
  'Anúncios por Semana': 'Anúncios p/ Semana',
  'Uso Offline': 'Uso Offline',
  'Alta Frustração': 'Alta Frustração',
  'Intensidade de Ads': 'Intensidade de Ads',
  'Baixo Tempo de Uso': 'Baixo Tempo de Uso',
  'Taxa de Pulos Elevada': 'Taxa de Pulagem',
  'Índice de Frustração Alto': 'Índice de Frustração',
  'Subutilização Premium': 'Premium sem Offline',
  'Tempo de Escuta Baixo': 'Tempo de Escuta',
}

export const normalizeFeatureKey = (term = '') => String(term).replace(/^num__|^cat__/, '')

export const translateFeature = (term, fallback = '') => {
  if (!term) return fallback
  const cleanKey = normalizeFeatureKey(term)
  return FEATURE_LABELS[cleanKey] || cleanKey
}

export const translateFeatureLoose = (term, fallback = 'Desconhecido') => {
  if (!term) return fallback
  const cleanKey = normalizeFeatureKey(term)
  return FEATURE_LABELS[cleanKey] || cleanKey.replace(/_/g, ' ')
}
