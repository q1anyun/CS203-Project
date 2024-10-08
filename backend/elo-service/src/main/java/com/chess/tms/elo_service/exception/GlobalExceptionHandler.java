package com.chess.tms.elo_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlayerHistoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePlayerHistoryNotFoundException(PlayerHistoryNotFoundException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value()); 
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<Map<String, Object>> handleRestClientException(RestClientException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value()); 
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidReasonException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidReasonException(InvalidReasonException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value()); 
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
