package com.chess.tms.tournament_service.exception;

public class TournamentDoesNotExistException extends RuntimeException {
    
    public TournamentDoesNotExistException(String msg) {
        super(msg);
    }

}