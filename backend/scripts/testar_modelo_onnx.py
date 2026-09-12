#!/usr/bin/env python3
"""
Script para testar o modelo ONNX com dados reais
"""

import onnxruntime as rt
import numpy as np
import json

def testar_modelo():
    print("\n" + "="*60)
    print("🧪 TESTE DO MODELO ONNX")
    print("="*60 + "\n")
    
    # Carregar modelo
    modelo_path = "src/main/resources/modelo_hackathon.onnx"
    print(f"📂 Carregando modelo: {modelo_path}")
    
    try:
        sess = rt.InferenceSession(modelo_path)
        print("✅ Modelo carregado com sucesso!\n")
    except Exception as e:
        print(f"❌ Erro ao carregar modelo: {e}")
        return
    
    # Informações do modelo
    print("="*60)
    print("📊 INFORMAÇÕES DO MODELO")
    print("="*60 + "\n")
    
    print("📥 Inputs:")
    for input_info in sess.get_inputs():
        print(f"  - Nome: {input_info.name}")
        print(f"    Shape: {input_info.shape}")
        print(f"    Tipo: {input_info.type}\n")
    
    print("📤 Outputs:")
    for output_info in sess.get_outputs():
        print(f"  - Nome: {output_info.name}")
        print(f"    Shape: {output_info.shape}")
        print(f"    Tipo: {output_info.type}\n")
    
    # Teste 1: Cliente com ALTO risco de churn
    print("="*60)
    print("🔴 TESTE 1: Cliente com ALTO RISCO de Churn")
    print("="*60 + "\n")
    
    # Features: age, listening_time, songs_played_per_day, skip_rate, 
    #           ads_listened_per_week, offline_listening, songs_per_minute,
    #           ad_intensity, frustration_index, is_heavy_user, premium_no_offline,
    #           engagement_score, frustration_index_v2, premium_value, 
    #           high_skip_low_time, free_heavy_ads, songs_per_hour
    
    cliente_alto_risco = np.array([[
        25,      # age
        50,      # listening_time (baixo)
        5,       # songs_played_per_day (baixo)
        0.8,     # skip_rate (alto)
        80,      # ads_listened_per_week (alto)
        0,       # offline_listening (não usa)
        0.1,     # songs_per_minute (baixo)
        1.6,     # ad_intensity (alto)
        0.64,    # frustration_index (alto)
        0,       # is_heavy_user (não)
        0,       # premium_no_offline
        0.05,    # engagement_score (baixo)
        0.512,   # frustration_index_v2 (alto)
        0,       # premium_value (baixo)
        1,       # high_skip_low_time (sim)
        1,       # free_heavy_ads (sim)
        3.0      # songs_per_hour (baixo)
    ]], dtype=np.float32)
    
    input_name = sess.get_inputs()[0].name
    result = sess.run(None, {input_name: cliente_alto_risco})
    
    label = result[0][0]
    prob_stay = result[1][0][0]
    prob_churn = result[1][0][1]
    
    print(f"Classe Predita: {label} ({'CHURN' if label == 1 else 'STAY'})")
    print(f"Probabilidade de Permanência: {prob_stay:.4f} ({prob_stay*100:.2f}%)")
    print(f"Probabilidade de Churn: {prob_churn:.4f} ({prob_churn*100:.2f}%)")
    print(f"Risco: {'🔴 ALTO' if prob_churn > 0.7 else '🟡 MÉDIO' if prob_churn > 0.4 else '🟢 BAIXO'}\n")
    
    # Teste 2: Cliente com BAIXO risco de churn
    print("="*60)
    print("🟢 TESTE 2: Cliente com BAIXO RISCO de Churn")
    print("="*60 + "\n")
    
    cliente_baixo_risco = np.array([[
        30,      # age
        500,     # listening_time (alto)
        50,      # songs_played_per_day (alto)
        0.2,     # skip_rate (baixo)
        0,       # ads_listened_per_week (zero - premium)
        1,       # offline_listening (usa)
        0.8,     # songs_per_minute (alto)
        0,       # ad_intensity (zero)
        0.04,    # frustration_index (baixo)
        1,       # is_heavy_user (sim)
        1,       # premium_no_offline
        0.9,     # engagement_score (alto)
        0.008,   # frustration_index_v2 (baixo)
        1,       # premium_value (alto)
        0,       # high_skip_low_time (não)
        0,       # free_heavy_ads (não)
        30.0     # songs_per_hour (alto)
    ]], dtype=np.float32)
    
    result = sess.run(None, {input_name: cliente_baixo_risco})
    
    label = result[0][0]
    prob_stay = result[1][0][0]
    prob_churn = result[1][0][1]
    
    print(f"Classe Predita: {label} ({'CHURN' if label == 1 else 'STAY'})")
    print(f"Probabilidade de Permanência: {prob_stay:.4f} ({prob_stay*100:.2f}%)")
    print(f"Probabilidade de Churn: {prob_churn:.4f} ({prob_churn*100:.2f}%)")
    print(f"Risco: {'🔴 ALTO' if prob_churn > 0.7 else '🟡 MÉDIO' if prob_churn > 0.4 else '🟢 BAIXO'}\n")
    
    # Teste 3: Cliente MÉDIO risco
    print("="*60)
    print("🟡 TESTE 3: Cliente com MÉDIO RISCO de Churn")
    print("="*60 + "\n")
    
    cliente_medio_risco = np.array([[
        28,      # age
        200,     # listening_time (médio)
        20,      # songs_played_per_day (médio)
        0.5,     # skip_rate (médio)
        30,      # ads_listened_per_week (médio)
        0,       # offline_listening
        0.4,     # songs_per_minute (médio)
        0.6,     # ad_intensity (médio)
        0.3,     # frustration_index (médio)
        0,       # is_heavy_user
        0,       # premium_no_offline
        0.4,     # engagement_score (médio)
        0.18,    # frustration_index_v2 (médio)
        0,       # premium_value
        0,       # high_skip_low_time
        1,       # free_heavy_ads
        12.0     # songs_per_hour (médio)
    ]], dtype=np.float32)
    
    result = sess.run(None, {input_name: cliente_medio_risco})
    
    label = result[0][0]
    prob_stay = result[1][0][0]
    prob_churn = result[1][0][1]
    
    print(f"Classe Predita: {label} ({'CHURN' if label == 1 else 'STAY'})")
    print(f"Probabilidade de Permanência: {prob_stay:.4f} ({prob_stay*100:.2f}%)")
    print(f"Probabilidade de Churn: {prob_churn:.4f} ({prob_churn*100:.2f}%)")
    print(f"Risco: {'🔴 ALTO' if prob_churn > 0.7 else '🟡 MÉDIO' if prob_churn > 0.4 else '🟢 BAIXO'}\n")
    
    print("="*60)
    print("✅ TESTES CONCLUÍDOS COM SUCESSO!")
    print("="*60 + "\n")

if __name__ == "__main__":
    try:
        testar_modelo()
    except Exception as e:
        print(f"\n❌ Erro durante os testes: {e}")
        import traceback
        traceback.print_exc()
