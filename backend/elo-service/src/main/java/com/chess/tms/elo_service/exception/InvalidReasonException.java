package com.chess.tms.elo_service.exception;

public class InvalidReasonException extends RuntimeException {
    public InvalidReasonException(String msg) {
        super(msg);
    }
}
