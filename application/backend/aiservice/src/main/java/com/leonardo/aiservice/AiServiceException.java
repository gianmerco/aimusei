package com.leonardo.aiservice;

public class AiServiceException extends RuntimeException {
    private final int code;
    private final String responseBody;

    public AiServiceException(String message, Exception cause, int code, String responseBody) {
        super(message, cause);
        this.code = code;
        this.responseBody = responseBody;
    }

    public AiServiceException(String message, Exception cause) {
        super(message, cause);
        this.code = -1;
        this.responseBody = null;
    }

    public int getCode() {
        return code;
    }

    public String getResponseBody() {
        return responseBody;
    }

}
