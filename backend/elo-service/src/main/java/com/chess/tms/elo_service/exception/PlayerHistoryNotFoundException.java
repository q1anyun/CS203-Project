package com.chess.tms.elo_service.exception;

/**
 * This exception is thrown when an EloHistory of given player id does not exist
 */
public class PlayerHistoryNotFoundException extends RuntimeException{
    public PlayerHistoryNotFoundException (String msg) {
        super(msg);
    }
}
