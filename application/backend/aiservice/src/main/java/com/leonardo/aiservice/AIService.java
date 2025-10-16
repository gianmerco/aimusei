package com.leonardo.aiservice;

/**
 * Interface to interact with the remote AI gateway service. 
 * Allows to send an AbstractRequest, of any type, and returns a generic AbstractResponse. 
 * Users should use the interface and not the subclasses.
 */
public interface AIService {

    /**
     * Sends an AbstractRequest to the service.
     * @return an abstract response, which depends on the given request
     */
    AbstractResponse sendRequest(AbstractRequest request);
}
