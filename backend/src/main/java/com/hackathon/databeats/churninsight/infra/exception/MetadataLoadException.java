package com.hackathon.databeats.churninsight.infra.exception;

/**
 * Exceção lançada quando os metadados do modelo não conseguem ser carregados.
 *
 * <p>Indica falha durante inicialização da aplicação ao tentar carregar
 * arquivo metadata.json com configurações do modelo de ML.</p>
 *
 * <p><b>Cenários:</b></p>
 * <ul>
 *   <li>Arquivo metadata.json não encontrado</li>
 *   <li>JSON inválido ou corrompido</li>
 *   <li>Campos obrigatórios faltando</li>
 * </ul>
 *
 * <p><b>Impacto:</b> Falha fatal - aplicação não inicia sem metadados válidos.</p>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
public class MetadataLoadException extends RuntimeException {

	/**
	 * Cria nova exceção com mensagem de erro.
	 *
	 * @param message descrição do erro ao carregar metadados
	 */
	public MetadataLoadException(String message) {
		super(message);
	}
}