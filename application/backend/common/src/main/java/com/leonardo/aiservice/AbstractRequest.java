package com.leonardo.aiservice;

/**
 * A generic request to the AI gateway service.
 * All requests have a String input, which corresponds to the user prompt.
 * Objects of the subclasses of AbstractRequest should be instantiated using the builder
 * pattern.
 */
public abstract class AbstractRequest {
    private final String input;

    protected AbstractRequest(String input) {
        this.input = input;
    }

    /**
     * @return the request input
     */
    public String getInput() {
        return input;
    }

}