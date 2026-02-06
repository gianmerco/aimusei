package com.leonardo.aiservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.leonardo.aiservice.internal.AiGatewayClient;
import com.leonardo.aiservice.internal.K8sApiException;
import com.leonardo.aiservice.request.AiRequest;
import com.leonardo.aiservice.response.AiResponse;

/**
 * implementazione standard di AiService. La libreria è aperta a ulteriori implementazioni in futuro
 */
public class DefaultAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(DefaultAiService.class);

    private final AiGatewayClient client;

    public DefaultAiService(AiGatewayClient client) {
        this.client = client;
    }

    @Override
    public AiResponse sendRequest(AiRequest request) {
        try {
            return client.processRequest(request);
        }
        // metto qui la gestione delle eccezioni del metodo processRequest
        catch(K8sApiException e) {
            // errori con la kubernetes token API
            log.error("Errore con la Kubernetes Token API: {}. Impossibile recuperare il token", e.getMessage());
            throw new AiServiceException("Autenticazione con client Kubernetes fallita", e, e.getCode(), e.getResponseBody());
        }
        catch (WebClientResponseException e) {
            // HTTP 4xx/5xx errors dal gateway
            HttpStatusCode status = e.getStatusCode();
            String body = e.getResponseBodyAsString();
            
            log.error("AI Gateway ha risposto con errore HTTP {}: {}", 
                      status.value(), body);
            
            throw new AiServiceException("AI Gateway error: " + status, e, status.value(), body);
        }
        catch (WebClientRequestException e) {
            // Errori di rete (DNS, connection refused, timeout)
            log.error("Errore di connessione all'AI Gateway: {}", e.getMessage());
            
            throw new AiServiceException("Impossibile connettersi all'AI Gateway: " + e.getMessage(), e, -1, null);
        }
    }
}
