package com.leonardo.aiservice;

import org.springframework.stereotype.Service;

/**
 * Implementation of the AIService interface.
 */
@Service
public class ConcreteAIService implements AIService {
    private final AIWebClient client;

    public ConcreteAIService(AIWebClient client) {
        this.client = client;
    }

    @Override
    public AbstractResponse sendRequest(AbstractRequest request) {
        if (request instanceof AIRequest aiRequest)
            return client.processRequest(aiRequest);
        else
            throw new IllegalArgumentException("Invalid request: not supported");
    }
}
