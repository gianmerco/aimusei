package com.leonardo.aiservice;

import java.net.URI;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import io.kubernetes.client.openapi.ApiException;

/**
 * Component responsible for sending actual requests to the AIGateway.
 */
@Component
@Primary
public class AIWebClient {
    private final TokenManager tokenManager;
    private final WebClient webClient;
    private final AIServiceProperties props;

    public AIWebClient(TokenManager tokenManager, WebClient webClient, AIServiceProperties props) {
        this.tokenManager = tokenManager;
        this.webClient = webClient;
        this.props = props;
    }

    @SuppressWarnings("UseSpecificCatch")
    public AIResponse processRequest(AIRequest req) {
        try {
            // stacchiamo un token dinamico dalla Kubernetes API utilizzando la classe apposita
            String serviceAccountToken = tokenManager.requestTokenWithAudience();
            System.out.println("ServiceAccount token retrieved successfully");

            // l'URL dell'AIGateway deve essere specificato come variabile d'ambiente all'interno dell'applicativo che fa uso della libreria (vedere classe AIServiceProperties)
            String uri = props.getUri() + props.getEndpoint();
            System.out.println("Sending AI request to: " + uri);

            // invio la richiesta allegando anche il token di autenticazione in un header speciale
            AIResponse result = webClient.post()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .header("SA-Token", serviceAccountToken) // va mantenuto allineato con il codice dell'AIGateway...
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(AIResponse.class)
                    .block();
            System.out.println("Message sent successfully. Response received successfully");

            return result;
        } 
        catch(ApiException e) {
            // catturo qui errori dovuti a problemi con il token
            System.out.println("Something went wrong while retrieving the token from the Kubernetes Token API.");
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            // catturiamo errori generici, potenzialmente URI errati, o errori lato server
            System.out.println("Something went wrong while sending the request to the AI gateway");
            throw new RuntimeException(e);
        }
    }
}
