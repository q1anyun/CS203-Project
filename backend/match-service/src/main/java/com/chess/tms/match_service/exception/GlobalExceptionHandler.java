package com.chess.tms.match_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Handle PlayerDoesNotExistInMatchException
    @ExceptionHandler(PlayerDoesNotExistInMatchException.class)
    public ResponseEntity<Map<String, Object>> handlePlayerDoesNotExistInMatchExceptionException(PlayerDoesNotExistInMatchException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());  // 404 Not Found status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //Handle MatchDoesNotExistException
    @ExceptionHandler(MatchDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleMatchDoesNotExistExceptionException(MatchDoesNotExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());  // 404 Not Found status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //Handle GameTypeNotFoundException
    @ExceptionHandler(GameTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameTypeNotFoundException(GameTypeNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //Handle RoundTypeNotFoundException
    @ExceptionHandler(RoundTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoundTypeNotFoundException(RoundTypeNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    //Handle MatchAlreadyCompleted
    @ExceptionHandler(MatchAlreadyCompletedException.class)
    public ResponseEntity<Map<String, Object>> handleMatchAlreadyCompleted(MatchAlreadyCompletedException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
