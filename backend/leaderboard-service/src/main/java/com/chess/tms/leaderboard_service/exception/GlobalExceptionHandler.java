package com.chess.tms.leaderboard_service.exception;

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
    @ExceptionHandler(EntryAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleEntryAlreadyExistsException(EntryAlreadyExistsException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.CONFLICT.value());  // 409 Conflict status
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    //Handle EntryDoesNotExistException
    @ExceptionHandler(EntryDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentDoesNotExistException(EntryDoesNotExistException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", e.getMessage());
        response.put("status", HttpStatus.NOT_FOUND.value());  // 404 Not Found status
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // @ExceptionHandler(UserDoesNotExistException.class) 
    // public ResponseEntity<Map<String, Object>> handleUserDoesNotExistException(UserDoesNotExistException e) {
    //     Map<String, Object> response = new HashMap<>();
    //     response.put("timestamp", LocalDateTime.now());
    //     response.put("message", e.getMessage());
    //     response.put("status", HttpStatus.NOT_FOUND.value());
    //     return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    // }
    

}
