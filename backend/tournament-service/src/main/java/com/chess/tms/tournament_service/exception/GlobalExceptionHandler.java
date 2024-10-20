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

    // Handle TournamentDoesNotExistException
    @ExceptionHandler(TournamentDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentDoesNotExistException(TournamentDoesNotExistException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle GameTypeNotFoundException
    @ExceptionHandler(GameTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGameTypeNotFoundException(GameTypeNotFoundException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle PlayerAlreadyRegisteredException
    @ExceptionHandler(PlayerAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> handlePlayerAlreadyRegisteredException(PlayerAlreadyRegisteredException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    // Handle MaxPlayersReachedException
    @ExceptionHandler(MaxPlayersReachedException.class)
    public ResponseEntity<Map<String, Object>> handleMaxPlayersReachedException(MaxPlayersReachedException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handle RoundTypeNotFoundException
    @ExceptionHandler(RoundTypeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoundTypeNotFoundException(RoundTypeNotFoundException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle TournamentAlreadyExistsException
    @ExceptionHandler(TournamentAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleTournamentAlreadyExistsException(TournamentAlreadyExistsException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.CONFLICT);
    }

    // Handle UserDoesNotExistException
    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<Map<String, Object>> handleUserDoesNotExistException(UserDoesNotExistException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Handle MatchServiceException
    @ExceptionHandler(MatchServiceException.class)
    public ResponseEntity<Map<String, Object>> handleMatchServiceException(MatchServiceException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle InsufficientPlayersException
    @ExceptionHandler(InsufficientPlayersException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPlayersException(InsufficientPlayersException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // Handle EloNotInRangeException
    @ExceptionHandler(EloNotInRangeException.class)
    public ResponseEntity<Map<String, Object>> handleEloNotInRangeException(EloNotInRangeException e) {
        return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("message", message);
        response.put("status", status.value());
        return new ResponseEntity<>(response, status);
    }
}