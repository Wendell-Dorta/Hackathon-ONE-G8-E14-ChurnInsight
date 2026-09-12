package com.hackathon.databeats.churninsight.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração do OpenAPI (Swagger) para documentação da API.
 *
 * <p>Define metadados da API, servidores disponíveis e organização de endpoints
 * por tags para melhor navegabilidade na documentação interativa.</p>
 *
 * <h3>Acesso à Documentação:</h3>
 * <ul>
 *   <li><b>Swagger UI:</b> {@code /swagger-ui.html}</li>
 *   <li><b>OpenAPI JSON:</b> {@code /v3/api-docs}</li>
 *   <li><b>OpenAPI YAML:</b> {@code /v3/api-docs.yaml}</li>
 * </ul>
 *
 * @author Equipe ChurnInsight
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {
    @Value("${app.public-base-url:http://localhost:10808}")
    private String publicBaseUrl;

    /**
     * Configura o documento OpenAPI com metadados da API ChurnInsight.
     *
     * @return configuração completa do OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .tags(buildTags());
    }

    /**
     * Constrói informações gerais da API.
     */
    private Info buildApiInfo() {
        return new Info()
                .title("ChurnInsight API")
                .version("1.0.0")
                .description("""
                    API de previsão de churn (cancelamento) de clientes utilizando Machine Learning.
                    
                    ## Funcionalidades
                    
                    - **Predição Individual**: Analisa um cliente e retorna probabilidade de cancelamento
                    - **Processamento em Lote**: Upload de CSV/XLSX para análise de múltiplos clientes
                    - **Diagnóstico de IA**: Explicação dos fatores de risco e ações sugeridas
                    - **Histórico de Predições**: Consulta paginada com filtros avançados
                    - **Estatísticas**: Métricas agregadas do modelo e predições
                    
                    ## Autenticação
                    
                    A API utiliza **HTTP Basic Authentication**. 
                    Credenciais padrão para desenvolvimento: `admin:Admin123`
                    
                    ## Rate Limiting
                    
                    - **50 requisições/segundo** por IP ou usuário autenticado
                    - **Burst capacity**: 100 requisições
                    - Headers retornados: `X-Rate-Limit-Limit`, `X-Rate-Limit-Remaining`
                    
                    ## Modelo de ML
                    
                    - **Algoritmo**: Logistic Regression com SMOTE
                    - **Framework**: ONNX Runtime (inferência em Java)
                    - **Threshold de decisão**: Configurável via metadata.json
                    """)
                .contact(new Contact()
                        .name("Equipe ChurnInsight - DataBeats")
                        .email("databeats@hackathon.com")
                        .url("https://github.com/databeats/churninsight"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * Constrói lista de servidores disponíveis.
     */
    private List<Server> buildServers() {
        return List.of(
                new Server()
                        .url(publicBaseUrl)
                        .description("Servidor público (host / porta exposta)"),
                new Server()
                        .url("http://churn-api:8080")
                        .description("Servidor Docker Compose (rede interna)"),
                new Server()
                        .url("https://api.churninsight.com")
                        .description("Servidor de Produção (quando disponível)")
        );
    }

    /**
     * Constrói tags para organização dos endpoints.
     */
    private List<Tag> buildTags() {
        return List.of(
                new Tag()
                        .name("Predição de Churn")
                        .description("Endpoints para predição individual e em lote de cancelamento de clientes"),
                new Tag()
                        .name("Client History")
                        .description("Consulta paginada e filtrada do histórico de predições"),
                new Tag()
                        .name("Client Predictions")
                        .description("Acesso às predições pré-calculadas do arquivo clients.json"),
                new Tag()
                        .name("API Contract")
                        .description("Especificação do contrato da API e exemplos de uso"),
                new Tag()
                        .name("Monitoramento")
                        .description("Health checks, métricas e status do modelo")
        );
    }
}