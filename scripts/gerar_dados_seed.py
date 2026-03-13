"""
Gera um CSV com dados de clientes para popular o banco via batch upload.
Uso: python scripts/gerar_dados_seed.py
"""
import csv
import random

random.seed(42)

GENDERS = ["Male", "Female", "Other"]
COUNTRIES = ["BR", "US", "DE", "CA", "GB", "FR", "AR", "MX", "PT", "ES"]
SUBSCRIPTION_TYPES = ["Free", "Premium", "Student", "Family"]
DEVICE_TYPES = ["Mobile", "Desktop", "Web"]

FIELDS = [
    "user_id", "gender", "age", "country", "subscription_type",
    "listening_time", "songs_played_per_day", "skip_rate",
    "ads_listened_per_week", "device_type", "offline_listening"
]

def gerar_cliente(i):
    sub = random.choice(SUBSCRIPTION_TYPES)
    is_free = sub == "Free"
    # Usuários free ouvem mais anúncios e pulam mais
    skip = round(random.uniform(0.3, 0.8) if is_free else random.uniform(0.05, 0.4), 2)
    ads = random.randint(5, 20) if is_free else random.randint(0, 3)
    offline = False if is_free else random.choice([True, False])
    listening = round(random.uniform(30, 300) if is_free else random.uniform(100, 700), 1)
    songs = random.randint(5, 20) if is_free else random.randint(10, 60)

    return {
        "user_id": f"user-{1000 + i}",
        "gender": random.choice(GENDERS),
        "age": random.randint(16, 55),
        "country": random.choice(COUNTRIES),
        "subscription_type": sub,
        "listening_time": listening,
        "songs_played_per_day": songs,
        "skip_rate": skip,
        "ads_listened_per_week": ads,
        "device_type": random.choice(DEVICE_TYPES),
        "offline_listening": str(offline).lower()
    }

output = "scripts/clientes_seed.csv"
n = 500

with open(output, "w", newline="", encoding="utf-8") as f:
    writer = csv.DictWriter(f, fieldnames=FIELDS)
    writer.writeheader()
    for i in range(n):
        writer.writerow(gerar_cliente(i))

print(f"✅ {n} clientes gerados em {output}")
