package com.leonardo.aiservice.internal;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import com.leonardo.aiservice.request.AiRequest;
import com.leonardo.aiservice.response.AiResponse;

/**
 * wrapper di web client per mandare le richieste al gateway, utilizzando le @AiServiceProperties
 */
public class AiGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(AiGatewayClient.class);

    private final TokenProvider tokenManager;
    private final WebClient webClient;
    private final String aiGatewayUri;
    private final String aiGatewayEndpoint;
    private final boolean k8sEnabled;

    public AiGatewayClient(TokenProvider tokenManager, WebClient webClient, String aiGatewayUri, String aiGatewayEndpoint, boolean k8sEnabled) {
        this.tokenManager = tokenManager;
        this.webClient = webClient;
        this.aiGatewayUri = aiGatewayUri;
        this.aiGatewayEndpoint = aiGatewayEndpoint;
        this.k8sEnabled = k8sEnabled;
    }

    public AiResponse processRequest(AiRequest requestBody) throws K8sApiException {
        // 1. stacchiamo un token dinamico dalla Kubernetes API utilizzando la classe apposita, se il kubernetes client è attivo
        String serviceAccountToken = "";
        if (k8sEnabled) {
            serviceAccountToken = tokenManager.requestToken();
            log.debug("Token ServiceAccount recuperato con successo");
        }

        // 2. calcoliamo l'URI del gateway
        String finalUri = aiGatewayUri.concat(aiGatewayEndpoint);
        log.info("Invio richiesta all'AI Gateway: {}", finalUri);

        // 3. costruiamo la richiesta e la inviamo
        RequestBodySpec requestSpec = webClient.post()
                    .uri(URI.create(finalUri))
                    .header("Content-Type", "application/json");
        if (k8sEnabled)
            requestSpec = requestSpec.header("SA-Token", serviceAccountToken);

        AiResponse result = requestSpec
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiResponse.class)
                .block();
        log.debug("Risposta ricevuta dall'AI Gateway");

        return result; 
    }
}
