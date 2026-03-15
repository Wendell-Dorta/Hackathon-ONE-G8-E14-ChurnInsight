"""
Gera dataset sintético com correlações realistas de churn para streaming de música.
Padrões baseados em literatura de churn prediction para serviços de assinatura.

Uso: python scripts/gerar_dataset_realista.py
Saída: scripts/spotify_churn_realista.csv
"""

import numpy as np
import pandas as pd

np.random.seed(42)
N = 10000

# --- Features base ---
age = np.random.randint(16, 65, N)
gender = np.random.choice(['Male', 'Female', 'Other'], N, p=[0.48, 0.48, 0.04])
country = np.random.choice(['US', 'UK', 'DE', 'FR', 'CA', 'AU', 'IN', 'PK'], N,
                            p=[0.30, 0.15, 0.12, 0.10, 0.10, 0.08, 0.08, 0.07])
subscription_type = np.random.choice(['Free', 'Premium', 'Family', 'Student'], N,
                                      p=[0.40, 0.35, 0.15, 0.10])
device_type = np.random.choice(['Mobile', 'Desktop', 'Web'], N, p=[0.55, 0.30, 0.15])

# --- Features comportamentais com distribuições realistas ---
listening_time = np.random.gamma(shape=3, scale=60, size=N).clip(10, 600)
songs_played_per_day = np.random.gamma(shape=4, scale=12, size=N).clip(1, 150).astype(int)
skip_rate = np.random.beta(2, 5, N)  # maioria tem skip_rate baixo
ads_listened_per_week = np.where(
    subscription_type == 'Free',
    np.random.poisson(12, N),   # Free ouve mais anúncios
    np.random.poisson(1, N)     # Premium quase nenhum
).clip(0, 40)
offline_listening = np.where(
    subscription_type == 'Free',
    np.random.binomial(1, 0.05, N),   # Free raramente usa offline
    np.random.binomial(1, 0.45, N)    # Premium usa mais
)

# --- Calcular churn probability com correlações realistas ---
# Cada fator contribui para a probabilidade de churn
logit = np.zeros(N)

# Skip rate alto → mais churn (fator mais importante)
logit += 4.0 * skip_rate

# Listening time baixo → mais churn
logit += -0.008 * listening_time

# Muitos anúncios → mais churn (especialmente Free)
logit += 0.08 * ads_listened_per_week

# Plano Free → mais churn
logit += np.where(subscription_type == 'Free', 1.2, 0.0)

# Sem offline → mais churn (para Premium)
logit += np.where((subscription_type != 'Free') & (offline_listening == 0), 0.5, 0.0)

# Poucas músicas por dia → mais churn
logit += -0.015 * songs_played_per_day

# Jovens e idosos → levemente mais churn
logit += 0.01 * np.abs(age - 30)

# Web → mais churn (menos engajamento)
logit += np.where(device_type == 'Web', 0.4, 0.0)

# Intercepto para calibrar ~25% churn rate
logit += -2.5

# Converter para probabilidade
prob_churn = 1 / (1 + np.exp(-logit))

# Adicionar ruído realista (nem todo comportamento é previsível)
prob_churn = prob_churn * 0.75 + np.random.uniform(0, 0.25, N)
prob_churn = prob_churn.clip(0, 1)

# Gerar target
is_churned = (np.random.uniform(0, 1, N) < prob_churn).astype(int)

# --- Montar DataFrame ---
df = pd.DataFrame({
    'user_id': [f'user_{i:05d}' for i in range(N)],
    'gender': gender,
    'age': age,
    'country': country,
    'subscription_type': subscription_type,
    'listening_time': listening_time.round(1),
    'songs_played_per_day': songs_played_per_day,
    'skip_rate': skip_rate.round(4),
    'device_type': device_type,
    'ads_listened_per_week': ads_listened_per_week,
    'offline_listening': offline_listening,
    'is_churned': is_churned,
})

# --- Validar correlações ---
print("✅ Dataset gerado com sucesso!")
print(f"   Total: {N} registros")
print(f"   Churn rate: {is_churned.mean():.1%}")
print()
print("📊 Correlações com churn (devem ser significativas):")
for col in ['skip_rate', 'listening_time', 'ads_listened_per_week', 'songs_played_per_day']:
    m1 = df[df['is_churned'] == 1][col].mean()
    m0 = df[df['is_churned'] == 0][col].mean()
    diff_pct = abs(m1 - m0) / m0 * 100
    print(f"   {col:30s} churn={m1:.3f}  stay={m0:.3f}  diff={diff_pct:.1f}%")

print()
print("📊 Churn por subscription_type:")
print(df.groupby('subscription_type')['is_churned'].mean().round(3).to_string())

output_path = 'scripts/spotify_churn_realista.csv'
df.to_csv(output_path, index=False)
print(f"\n💾 Salvo em: {output_path}")
print(f"   Use este CSV no Colab para treinar o modelo.")
