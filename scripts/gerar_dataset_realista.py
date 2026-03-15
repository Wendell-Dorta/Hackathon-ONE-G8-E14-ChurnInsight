"""
Gera dataset sintético com correlações fortes e realistas de churn para streaming de música.
Versão 2.0 — correlações mais fortes, interações entre features, 15k registros.

Uso: python scripts/gerar_dataset_realista.py
Saída: scripts/spotify_churn_realista.csv
"""

import numpy as np
import pandas as pd

np.random.seed(42)
N = 15000

# --- Features base ---
age = np.random.randint(16, 65, N)
gender = np.random.choice(['Male', 'Female', 'Other'], N, p=[0.48, 0.48, 0.04])
country = np.random.choice(['US', 'UK', 'DE', 'FR', 'CA', 'AU', 'IN', 'PK'], N,
                            p=[0.30, 0.15, 0.12, 0.10, 0.10, 0.08, 0.08, 0.07])
subscription_type = np.random.choice(['Free', 'Premium', 'Family', 'Student'], N,
                                      p=[0.40, 0.35, 0.15, 0.10])
device_type = np.random.choice(['Mobile', 'Desktop', 'Web'], N, p=[0.55, 0.30, 0.15])

is_free = (subscription_type == 'Free')
is_premium = (subscription_type == 'Premium')
is_family = (subscription_type == 'Family')

# --- Features comportamentais com distribuições por segmento ---
# Usuários Free ouvem menos e pulam mais
listening_time = np.where(
    is_free,
    np.random.gamma(2, 50, N).clip(10, 400),
    np.random.gamma(4, 60, N).clip(30, 600)
)

songs_played_per_day = np.where(
    is_free,
    np.random.gamma(3, 10, N).clip(1, 80).astype(int),
    np.random.gamma(5, 12, N).clip(5, 150).astype(int)
)

skip_rate = np.where(
    is_free,
    np.random.beta(3, 4, N),    # Free: skip_rate mais alto
    np.random.beta(2, 6, N)     # Premium: skip_rate mais baixo
)

ads_listened_per_week = np.where(
    is_free,
    np.random.poisson(15, N).clip(0, 50),
    np.random.poisson(1, N).clip(0, 5)
)

offline_listening = np.where(
    is_free,
    np.random.binomial(1, 0.03, N),
    np.where(is_family,
             np.random.binomial(1, 0.60, N),
             np.random.binomial(1, 0.45, N))
)

# --- Calcular churn com correlações fortes e interações ---
logit = np.zeros(N)

# Skip rate alto → forte sinal de churn
logit += 5.5 * skip_rate

# Listening time baixo → churn
logit += -0.012 * listening_time

# Muitos anúncios → churn (efeito amplificado para Free)
logit += 0.10 * ads_listened_per_week
logit += np.where(is_free, 0.04 * ads_listened_per_week, 0)  # interação Free×ads

# Plano Free → mais churn base
logit += np.where(is_free, 1.5, 0.0)
logit += np.where(device_type == 'Web', 0.6, 0.0)

# Premium sem offline → insatisfação
logit += np.where(is_premium & (offline_listening == 0), 0.7, 0.0)

# Poucas músicas por dia → menos engajamento
logit += -0.018 * songs_played_per_day

# Interação: skip_rate alto + Free = muito mais churn
logit += np.where(is_free & (skip_rate > 0.5), 1.2, 0.0)

# Interação: listening_time baixo + muitos anúncios = churn quase certo
logit += np.where((listening_time < 60) & (ads_listened_per_week > 10), 1.5, 0.0)

# Heavy users raramente cancelam
is_heavy = (listening_time > 400) & (skip_rate < 0.2)
logit += np.where(is_heavy, -2.0, 0.0)

# Intercepto para ~28% churn rate
logit += -3.2

# Probabilidade com ruído controlado (70% sinal, 30% ruído)
prob_churn = 1 / (1 + np.exp(-logit))
prob_churn = prob_churn * 0.70 + np.random.uniform(0, 0.30, N)
prob_churn = prob_churn.clip(0, 1)

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
print("✅ Dataset v2.0 gerado!")
print(f"   Total: {N} registros")
print(f"   Churn rate: {is_churned.mean():.1%}")
print()
print("📊 Correlações com churn (quanto maior o diff%, melhor o sinal):")
for col in ['skip_rate', 'listening_time', 'ads_listened_per_week', 'songs_played_per_day']:
    m1 = df[df['is_churned'] == 1][col].mean()
    m0 = df[df['is_churned'] == 0][col].mean()
    diff_pct = abs(m1 - m0) / (m0 + 1e-9) * 100
    print(f"   {col:30s} churn={m1:.3f}  stay={m0:.3f}  diff={diff_pct:.1f}%")

print()
print("📊 Churn por subscription_type:")
print(df.groupby('subscription_type')['is_churned'].mean().round(3).to_string())

output_path = 'scripts/spotify_churn_realista.csv'
df.to_csv(output_path, index=False)
print(f"\n💾 Salvo em: {output_path}")
