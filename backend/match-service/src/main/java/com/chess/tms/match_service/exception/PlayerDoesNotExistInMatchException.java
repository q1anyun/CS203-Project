package com.chess.tms.match_service.exception;

public class PlayerDoesNotExistInMatchException extends RuntimeException {
    
    public PlayerDoesNotExistInMatchException(String msg) {
        super(msg);
    }

}