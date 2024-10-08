package com.chess.tms.elo_service.exception;

public class PlayerHistoryNotFoundException extends RuntimeException{
    public PlayerHistoryNotFoundException (String msg) {
        super(msg);
    }
}
