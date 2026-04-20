package it.prismaprogetti.aimusei.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import com.leonardo.aiservice.AiServiceException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AiServiceException.class)
    public ResponseEntity<ErrorResponse> handleAiServiceException(AiServiceException ex, WebRequest request) {
        int statusCode = ex.getCode();

        // Se lo status code è 503, rispondiamo con 503
        if (statusCode == 503) {
            ErrorResponse error = new ErrorResponse(
                503,
                ex.getMessage(),
                ex.getResponseBody()
            );
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }

        HttpStatus httpStatus;
        if (statusCode >= 400 && statusCode < 500) {
            httpStatus = HttpStatus.valueOf(statusCode);
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse error = new ErrorResponse(
            statusCode,
            ex.getMessage(),
            ex.getResponseBody()
        );
        return ResponseEntity.status(httpStatus).body(error);
    }
    public record ErrorResponse(int code, String message, String responseBody) {}
}