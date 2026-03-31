package com.leonardo.aiservice;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * questa classe contiene informazioni di configurazione per permettere la comunicazione con il AI gateway. Consultare il README.md per maggiori informazioni
 */
@ConfigurationProperties(prefix = "aiservice")
public record AiServiceProperties(
    @DefaultValue("http://localhost:8080")
    String uri,
    @DefaultValue("/aiservice/airequest")
    String endpoint,
    @DefaultValue("500")
    Long timeoutSeconds,
    @DefaultValue
    K8sProperties k8s
) {
    public record K8sProperties(
        @DefaultValue("true")
        boolean enabled,
        @DefaultValue("default")
        String namespace, 
        @DefaultValue("default")
        String serviceAccount) {}  
}