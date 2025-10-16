package com.leonardo.aiservice;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This class contains as properties some configuration information that the library needs to run.
 * In particular, the library needs to now:
 * 1. the URL of the AIGateway service to talk to (via the AI_GATEWAY_URI environmental variable; default is http://aigateway-middleware.aigateway.svc.cluster.local:80, which should work already)
 * 2. the AIGateway endpoint to send requests to (via the AI_GATEWAY_ENDPOINT environmental variable; default is /aiservice/airequest, which works with current AIGateway implementation)
 * 3. the Kubernetes ServiceAccount name associated to the workload which is using the library (no default, must be set using K8S_CLIENT_SERVICE_ACCOUNT_NAME)
 * 4. the Kubernetes namespace of the workload using the library (no default, set K8S_CLIENT_NAMESPACE)
 */
@ConfigurationProperties(prefix = "aiservice")
public class AIServiceProperties {
    private String uri = "http://aigateway-middleware.accessibilita.svc.cluster.local:80";
    private String endpoint = "/aiservice/airequest";
    private final String k8sServiceAccount;
    private final String k8sNamespace;

    public AIServiceProperties() {
        String envUri = System.getenv("AI_GATEWAY_URI");
        String envEndpoint = System.getenv("AI_GATEWAY_ENDPOINT");
        
        if (envUri != null && !envUri.trim().isEmpty()) this.uri = envUri;
        if (envEndpoint != null && !envEndpoint.trim().isEmpty()) this.endpoint = envEndpoint;

        this.k8sNamespace = System.getenv("K8S_CLIENT_NAMESPACE");
        this.k8sServiceAccount = System.getenv("K8S_CLIENT_SERVICE_ACCOUNT_NAME");
    }

    public String getUri() {
        return uri;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getK8sServiceAccount() {
        return k8sServiceAccount;
    }

    public String getK8sNamespace() {
        return k8sNamespace;
    }
    
}
