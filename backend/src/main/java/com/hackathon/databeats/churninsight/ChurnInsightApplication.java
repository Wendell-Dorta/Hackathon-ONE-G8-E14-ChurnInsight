package com.hackathon.databeats.churninsight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada da aplicação ChurnInsight.
 *
 * <p>Inicializa a aplicação Spring Boot com as seguintes capacidades:</p>
 * <ul>
 *   <li>AutoConfiguration automática de beans Spring</li>
 *   <li>Agendamento de tarefas periódicas</li>
 *   <li>Integração com serviços de ML (ONNX Runtime)</li>
 *   <li>Persistência em banco de dados MySQL</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class ChurnInsightApplication {

	/**
	 * Método principal de inicialização da aplicação.
	 *
	 * @param args argumentos de linha de comando (ignorados neste contexto)
	 */
	public static void main(String[] args) {
		SpringApplication.run(ChurnInsightApplication.class, args);
	}
}
