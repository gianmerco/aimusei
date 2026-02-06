package com.leonardo.aiservice.internal;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.fabric8.kubernetes.api.model.authentication.TokenRequest;
import io.fabric8.kubernetes.api.model.authentication.TokenRequestBuilder;
import io.fabric8.kubernetes.api.model.authentication.TokenRequestStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;

/**
* Questa classe interagisce con l'API Kubernetes (in particolare con la TokenAPI) e recupera
* un token. Questo token è 1) vincolato nel tempo (10 minuti); 2) vincolato nell'audience 
* (ovvero "aigateway"). L'audience è necessaria in quanto "contrassegna" il
* token come destinato ad AIGateway e gli fa superare il processo di autenticazione.
*
* Affinché questa classe funzioni, il workload che utilizza questa libreria deve avere l'autorizzazione
* a richiedere token all'API Kubernetes. Ciò significa in pratica che il suo service account
* deve essere associata a un ClusterRole con regola "create" su "serviceacounts/token".
*/
public class KubernetesTokenManager implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(KubernetesTokenManager.class);

    private final KubernetesClient kubernetesClient;
    private final String namespace;
    private final String serviceAccountName;

    public KubernetesTokenManager(KubernetesClient kubernetesClient, String namespace, String serviceAccountName) {
        this.kubernetesClient = kubernetesClient;
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
        this.serviceAccountName = Objects.requireNonNull(serviceAccountName, "serviceAccountName must not be null");
    }

    /**
     * Richiede un token con audience "aigateway", utilizzando il service account nel namespace forniti alla classe
     * @return la stringa token
     * @throws K8sApiException se la chiamata alla Kubernetes API fallisce (è una RuntimeException)
     */
    @Override
    public String requestToken() {
        log.debug("Richiesta token per ServiceAccount {}/{} in corso", namespace, serviceAccountName);

        TokenRequest tokenRequest = new TokenRequestBuilder()
                .withNewSpec()
                    .withAudiences("aigateway")
                    .withExpirationSeconds(600L)
                .endSpec()
                .build();

        try {
            TokenRequest result = kubernetesClient.serviceAccounts()
                    .inNamespace(namespace)
                    .withName(serviceAccountName)
                    .tokenRequest(tokenRequest);

            if (result == null) {
                throw new K8sApiException("ServiceAccount " + serviceAccountName + " non trovato nel namespace " + namespace);
            }
            
            TokenRequestStatus status = result.getStatus();
            if (status == null || status.getToken() == null) {
                throw new K8sApiException("Impossibile ottenere un token dall'API Kubernetes: status=" + status);
            }

            log.debug("Token ottenuto con scadenza {} secondi", 600L);
            return status.getToken();
        }
        catch (KubernetesClientException e) {
            log.error("Errore nella richiesta token Kubernetes: code={}, body={}", e.getCode(), e.getMessage(), e);
            throw new K8sApiException(e.getCode(), e.getMessage(), e.getMessage(), e);
        }
    }
}
