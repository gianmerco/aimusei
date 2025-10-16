package com.leonardo.aiservice;

import java.io.IOException;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;

@AutoConfiguration
@EnableConfigurationProperties(AIServiceProperties.class)
public class AIServiceConfiguration {

    @Bean
    public AIService aiService(AIWebClient aiWebClient) {
        return new ConcreteAIService(aiWebClient);
    }

    @Bean
    public AIWebClient aiWebClient(TokenManager tokenManager, WebClient webClient, AIServiceProperties props) {
        return new AIWebClient(tokenManager, webClient, props);
    }

    @Bean
    public TokenManager tokenManager(ApiClient apiClient, AIServiceProperties props) {
        return new TokenManager(apiClient, props);
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    // Kubernetes beans; qui configuriamo il client per parlare con la Kubernetes API, usiamo la configurazione di default

    @Bean
    public ApiClient kubernetesApiClient() throws IOException {
        try {
            System.out.println("\tSetting up Kubernetes API client with in-cluster configuration");
            ApiClient client = Config.defaultClient();
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
            System.out.println("\tSet up of Kubernetes API client completed");
            return client;
        }
        catch(Exception e) {
            System.out.println("\tNo kubernetes client created; continue with test mode");
            return new ApiClient();
        }
    }
}
