package com.leonardo.aiservice;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.AuthenticationV1TokenRequest;
import io.kubernetes.client.openapi.models.V1TokenRequestSpec;
import io.kubernetes.client.openapi.models.V1TokenRequestStatus;

/**
 * This class interacts with the Kubernetes API (in particular with the TokenAPI) and retrieves
 * a token. This token is 1) bound in time (10 minutes); 2) bound in audience (meaning it has a
 * specific audience associated, namely "aigateway"). The audience is needed as it "marks" the
 * token as intended for the AIGateway and makes it pass the authentication process.
 * 
 * In order for this class to work, the deployment's workload using this library must have the
 * authorization to ask for tokens to the Kubernetes API. This in practice means that its SA
 * should be associated with a ClusterRole with rule verb "create" over "serviceacounts/token"
 * resources.
 */
@Component
@Primary
public class TokenManager {
    private final CoreV1Api coreV1Api; // vedi AIServiceConfiguration; oggetto client per parlare con Kubernetes
    private final String namespace; // per richiedere un token, è necessario specificare il service account a cui fa riferimento; per questo ci serve il nome del SA e il namespace di appartenenza
    private final String serviceAccountName;

    public TokenManager(ApiClient apiClient, AIServiceProperties props) {
        this.coreV1Api = new CoreV1Api(apiClient);
        this.namespace = props.getK8sNamespace();
        this.serviceAccountName = props.getK8sServiceAccount();
    }

    /**
     * Requests a token for the service account with the "aigateway" audience.
     * @return the token string
     * @throws ApiException if the Kubernetes API call fails
     */
    public String requestTokenWithAudience() throws ApiException {
        // definisico le specifiche per il token (audience e expiration time)
        V1TokenRequestSpec spec = new V1TokenRequestSpec();
        spec.setAudiences(java.util.Collections.singletonList("aigateway"));
        spec.setExpirationSeconds(600L);

        // creo l'oggetto token request e gli associo le specifiche definite
        AuthenticationV1TokenRequest tokenRequest = new AuthenticationV1TokenRequest();
        tokenRequest.setSpec(spec);

        try {
            // chiamo l'API per staccare un token per il service account specificato e con le caratteristiche definite sopra. Questa richiesta può fallire generando una ApiException (se ci sono stati problemi di configurazione del client o il workload che usa la libreria non ha i permessi adatti)
            AuthenticationV1TokenRequest result = coreV1Api.createNamespacedServiceAccountToken(
                    serviceAccountName,
                    namespace,
                    tokenRequest,
                    null,
                    null,
                    null,
                    null
            );

            // check del risultato
            V1TokenRequestStatus status = result.getStatus();
            if (status == null || status.getToken() == null) {
                throw new ApiException("Failed to obtain token from Kubernetes API");
            }

            return status.getToken();
        }
        catch (ApiException e) {
            System.out.println("No Kubernetes client was configured.");
            System.out.println(e.getCode());
            System.out.println(e.getResponseHeaders());
            System.out.println(e.getResponseBody());
            throw new RuntimeException(e);
        }
    }
}

