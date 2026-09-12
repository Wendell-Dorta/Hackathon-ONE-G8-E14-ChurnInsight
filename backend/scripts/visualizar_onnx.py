#!/usr/bin/env python3
"""
Script para visualizar informações de um modelo ONNX
"""

import onnx
import sys
from pathlib import Path

def visualizar_modelo_onnx(caminho_modelo):
    """Exibe informações detalhadas do modelo ONNX"""
    
    print(f"\n{'='*60}")
    print(f"📊 ANÁLISE DO MODELO ONNX")
    print(f"{'='*60}\n")
    
    # Carregar modelo
    modelo = onnx.load(caminho_modelo)
    
    # Informações básicas
    print(f"📁 Arquivo: {Path(caminho_modelo).name}")
    print(f"📏 Tamanho: {Path(caminho_modelo).stat().st_size / 1024:.2f} KB")
    print(f"🔢 Versão ONNX: {modelo.opset_import[0].version}")
    print(f"🏷️  Producer: {modelo.producer_name}")
    print(f"📝 Versão Producer: {modelo.producer_version}")
    
    # Grafo
    grafo = modelo.graph
    print(f"\n{'='*60}")
    print(f"🔗 ESTRUTURA DO GRAFO")
    print(f"{'='*60}\n")
    print(f"Nome: {grafo.name}")
    print(f"Nós (operações): {len(grafo.node)}")
    
    # Inputs
    print(f"\n{'='*60}")
    print(f"📥 INPUTS DO MODELO")
    print(f"{'='*60}\n")
    for i, input_tensor in enumerate(grafo.input, 1):
        print(f"{i}. {input_tensor.name}")
        if input_tensor.type.tensor_type.shape.dim:
            dims = [d.dim_value if d.dim_value > 0 else 'dynamic' 
                   for d in input_tensor.type.tensor_type.shape.dim]
            print(f"   Shape: {dims}")
        print(f"   Tipo: {onnx.TensorProto.DataType.Name(input_tensor.type.tensor_type.elem_type)}")
    
    # Outputs
    print(f"\n{'='*60}")
    print(f"📤 OUTPUTS DO MODELO")
    print(f"{'='*60}\n")
    for i, output_tensor in enumerate(grafo.output, 1):
        print(f"{i}. {output_tensor.name}")
        if output_tensor.type.tensor_type.shape.dim:
            dims = [d.dim_value if d.dim_value > 0 else 'dynamic' 
                   for d in output_tensor.type.tensor_type.shape.dim]
            print(f"   Shape: {dims}")
        print(f"   Tipo: {onnx.TensorProto.DataType.Name(output_tensor.type.tensor_type.elem_type)}")
    
    # Operações
    print(f"\n{'='*60}")
    print(f"⚙️  OPERAÇÕES DO MODELO")
    print(f"{'='*60}\n")
    
    ops = {}
    for node in grafo.node:
        ops[node.op_type] = ops.get(node.op_type, 0) + 1
    
    for op, count in sorted(ops.items(), key=lambda x: x[1], reverse=True):
        print(f"  {op}: {count}x")
    
    # Inicializadores (pesos)
    print(f"\n{'='*60}")
    print(f"⚖️  PARÂMETROS DO MODELO")
    print(f"{'='*60}\n")
    print(f"Total de tensores inicializados: {len(grafo.initializer)}")
    
    total_params = 0
    for init in grafo.initializer:
        params = 1
        for dim in init.dims:
            params *= dim
        total_params += params
    
    print(f"Total de parâmetros: {total_params:,}")
    print(f"Tamanho estimado: {total_params * 4 / 1024 / 1024:.2f} MB (float32)")
    
    # Validar modelo
    print(f"\n{'='*60}")
    print(f"✅ VALIDAÇÃO")
    print(f"{'='*60}\n")
    try:
        onnx.checker.check_model(modelo)
        print("✅ Modelo ONNX válido!")
    except Exception as e:
        print(f"❌ Erro na validação: {e}")
    
    print(f"\n{'='*60}\n")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: python visualizar_onnx.py <caminho_do_modelo.onnx>")
        print("\nExemplo:")
        print("  python visualizar_onnx.py ../src/main/resources/modelo_hackathon.onnx")
        sys.exit(1)
    
    caminho = sys.argv[1]
    
    if not Path(caminho).exists():
        print(f"❌ Arquivo não encontrado: {caminho}")
        sys.exit(1)
    
    try:
        visualizar_modelo_onnx(caminho)
    except Exception as e:
        print(f"❌ Erro ao processar modelo: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
