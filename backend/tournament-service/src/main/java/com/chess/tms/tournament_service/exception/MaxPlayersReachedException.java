package com.chess.tms.tournament_service.exception;

public class MaxPlayersReachedException extends RuntimeException {
    public MaxPlayersReachedException(String message) {
        super(message);
    }
}