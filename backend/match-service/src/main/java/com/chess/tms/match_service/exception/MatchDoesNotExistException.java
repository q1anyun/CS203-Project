package com.chess.tms.match_service.exception;

public class MatchDoesNotExistException extends RuntimeException {
    public MatchDoesNotExistException(String msg) {
        super(msg);
    }
}
