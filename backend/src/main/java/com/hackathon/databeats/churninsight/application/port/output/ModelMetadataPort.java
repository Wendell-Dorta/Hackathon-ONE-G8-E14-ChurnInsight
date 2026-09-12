package com.hackathon.databeats.churninsight.application.port.output;

/**
 * Port de saída para acesso aos metadados do modelo de ML.
 *
 * <p>Define o contrato para obtenção das configurações e métricas do modelo,
 * isolando a camada de aplicação dos detalhes de implementação.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public interface ModelMetadataPort {

    /**
     * Obtém o threshold ótimo para classificação de churn.
     *
     * @return valor do threshold (0.0 a 1.0)
     */
    double getThresholdOtimo();

    /**
     * Obtém o nome do modelo.
     *
     * @return nome identificador do modelo
     */
    String getNomeModelo();

    /**
     * Obtém a versão do modelo.
     *
     * @return versão no formato semântico
     */
    String getVersaoModelo();

    /**
     * Obtém a acurácia do modelo.
     *
     * @return acurácia (0.0 a 1.0)
     */
    double getAcuracia();

    /**
     * Obtém o recall do modelo.
     *
     * @return recall (0.0 a 1.0)
     */
    double getRecall();

    /**
     * Obtém a precisão do modelo.
     *
     * @return precisão (0.0 a 1.0)
     */
    double getPrecisao();

    /**
     * Obtém o F1-Score do modelo.
     *
     * @return f1-score (0.0 a 1.0)
     */
    double getF1Score();

    /**
     * Obtém o AUC-ROC do modelo.
     *
     * @return auc-roc (0.0 a 1.0)
     */
    double getAucRoc();
}

