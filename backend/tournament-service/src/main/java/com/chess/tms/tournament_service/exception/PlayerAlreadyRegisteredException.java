package com.chess.tms.tournament_service.exception;

public class PlayerAlreadyRegisteredException extends RuntimeException {
    public PlayerAlreadyRegisteredException(String message) {
        super(message);
    }
}