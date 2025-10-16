package it.prismaprogetti.aimusei.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import it.prismaprogetti.aimusei.exception.InvalidOpenAIKeyException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice(assignableTypes = { BackofficeController.class , AccessibilityController.class, PublicController.class})
public class ExceptionHandling {

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
		log.error(e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
	}

	@ExceptionHandler(InvalidOpenAIKeyException.class)
	public ResponseEntity<?> handleInvalidOpenAIKeyException(InvalidOpenAIKeyException e) {
		log.error(e.getMessage(), e);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
	}
}