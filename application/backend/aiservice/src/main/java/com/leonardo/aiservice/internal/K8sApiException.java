package com.leonardo.aiservice.internal;

/**
 * eccezione custom per indicare un errore nella kubernetes (token) API
 */
public class K8sApiException extends RuntimeException {

    private final int code;
    private final String responseBody;

    public K8sApiException(String message) {
        super(message);
        this.code = 0;
        this.responseBody = null;
    }

    public K8sApiException(int code, String message, String responseBody, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.responseBody = responseBody;
    }

    public int getCode() {
        return code;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
