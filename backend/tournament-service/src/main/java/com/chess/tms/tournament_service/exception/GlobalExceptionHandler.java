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

    //Handle TournamentDoesNotExistException
    @ExceptionHandler(TournamentDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentDoesNotExistException(TournamentDoesNotExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());  // 404 Not Found status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(GameTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameTypeNotFoundException(GameTypeNotFoundException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlayerAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handlePlayerAlreadyRegistered(PlayerAlreadyRegisteredException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("timestamp", LocalDateTime.now());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


}
