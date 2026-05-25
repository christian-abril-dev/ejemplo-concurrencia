package com.abril.concurrencia.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleBadRequest(
			IllegalArgumentException ex, HttpServletRequest request){
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
			"timestamp", LocalDateTime.now().toString(),
			"status", 400,
			"error", ex.getMessage(),
			"path", request.getRequestURI()
		));
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneral(
			Exception ex, HttpServletRequest request){
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
			"timestamp", LocalDateTime.now().toString(),
			"status", 500, 
			"error", "Error interno del servidor",
			"path", request.getRequestURI()
		));
	}

}
