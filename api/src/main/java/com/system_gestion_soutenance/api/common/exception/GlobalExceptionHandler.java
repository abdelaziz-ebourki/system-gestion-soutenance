package com.system_gestion_soutenance.api.common.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
		String reason = ex.getReason();
		return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", reason != null ? reason : "Erreur"));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage()).reduce((a, b) -> a + "; " + b)
				.orElse("Erreur de validation");
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
	}
}
