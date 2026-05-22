package com.leonardo.aiservice.internal;

import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.leonardo.aiservice.request.AiRequest;
import com.leonardo.aiservice.response.AiResponse;

import reactor.util.retry.Retry;

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
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(3))
                        .filter(AiGatewayClient::isRetryable)
                        .doBeforeRetry(rs -> log.warn("Retry AI Gateway (tentativo #{}): {}",
                                rs.totalRetries() + 1,
                                rs.failure() == null ? "null" : rs.failure().toString())))
                .block();
        log.debug("Risposta ricevuta dall'AI Gateway");

        return result;
    }

    private static boolean isRetryable(Throwable t) {
        if (t == null) return false;
        if (t instanceof WebClientResponseException wcre) {
            int status = wcre.getStatusCode().value();
            // retry su body vuoto/troncato (200 + empty) e su 5xx
            if (status == 200) return true;
            return status >= 500 && status < 600;
        }
        // network: ConnectException, PrematureCloseException, ReadTimeoutException, etc.
        String name = t.getClass().getName();
        return name.contains("PrematureClose")
                || name.contains("ReadTimeout")
                || name.contains("WebClientRequestException")
                || name.contains("ConnectException")
                || name.contains("IOException");
    }
}
