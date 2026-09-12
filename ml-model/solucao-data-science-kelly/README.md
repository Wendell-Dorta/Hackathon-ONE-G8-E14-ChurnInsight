ChurnInsight for Spotify - Previsão de Churn
📊 Objetivo
Prever cancelamento de assinaturas do Spotify com pipeline completo de Machine Learning, da análise à implantação.

⚙️ Tecnologias
Python, Pandas, NumPy

Scikit-learn, XGBoost, Imbalanced-learn

Joblib (serialização) & ONNX (skl2onnx/onnxruntime para produção)

🎯 Resultados
Modelo: Random Forest otimizado via GridSearchCV

Desempenho: 86% acurácia | 87% recall (classe churn)

Features Importantes: num_colaboradores, estado_cliente, total_gorjetas_fiscais

Serialização: Modelo salvo em Joblib (.pkl) e convertível para ONNX (.onnx) para implantação eficiente

🚀 Como Usar

git clone https://github.com/KellyMuehlmann/Hackathon-ONE---Churn-clientes.git
cd Hackathon-ONE-Churn-clientes
pip install -r requirements.txt
jupyter notebook ChurnInsight_for_Spotify_*.ipynb

Para implantação: Use o modelo .pkl (Joblib) ou converta para .onnx para otimização em produção.

👥 Equipe
Projeto desenvolvido pela Equipe de Data Science para o Hackathon ONE II Brasil (Oracle Next Education).

Pipeline de ML para previsão de churn com suporte a Joblib e ONNX para implantação.
