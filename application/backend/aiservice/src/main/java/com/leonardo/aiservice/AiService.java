package com.leonardo.aiservice;

import com.leonardo.aiservice.request.AiRequest;
import com.leonardo.aiservice.response.AiResponse;

/**
 * rappresenta un servizio ai gateway remoto, cui mandare generiche richieste e ricevere una generica risposta in cambio.
 * A seconda del tipo di richiesta che mando, otterrò una risposta differente
 */
public interface AiService {

    /**
     * invio una AiRequest generica al gateway.
     * @return una AiResponse, il cui tipo dipende dalla richiesta in input
     */
    AiResponse sendRequest(AiRequest request);
}
