package com.chess.tms.tournament_service.exception;

public class UserDoesNotExistException extends RuntimeException {
    
    public UserDoesNotExistException(String msg) {
        super(msg);
    }
    
}
