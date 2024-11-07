package com.chess.tms.elo_service.exception;

/**
 * This exception is thrown when a reason given is invalid.
 */
public class InvalidReasonException extends RuntimeException {
    public InvalidReasonException(String msg) {
        super(msg);
    }
}
