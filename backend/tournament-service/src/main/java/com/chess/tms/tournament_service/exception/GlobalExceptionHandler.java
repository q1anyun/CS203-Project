package com.chess.tms.tournament_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    //Handle TournamentAlreadyExistsException
    @ExceptionHandler(TournamentAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentAlreadyExistsException(TournamentAlreadyExistsException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());  // 409 Conflict status
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //Handle TournamentDoesNotExistException
    @ExceptionHandler(TournamentDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentDoesNotExistException(TournamentDoesNotExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());  // 404 Not Found status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserDoesNotExistException.class) 
    public ResponseEntity<Map<String, Object>> handleUserDoesNotExistException(UserDoesNotExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    

}
