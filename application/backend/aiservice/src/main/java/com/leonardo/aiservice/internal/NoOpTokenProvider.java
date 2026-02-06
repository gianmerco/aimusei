package com.leonardo.aiservice.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpTokenProvider implements TokenProvider {

    private static final Logger log = LoggerFactory.getLogger(NoOpTokenProvider.class);

    @Override
    public String requestToken() {
        log.warn("TokenProvider NoOp attivo: nessun token Kubernetes Service Account è stato generato per la richiesta corrente");
        return "";
    }

}
