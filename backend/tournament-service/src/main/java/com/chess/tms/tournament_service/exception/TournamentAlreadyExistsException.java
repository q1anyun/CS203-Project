package com.chess.tms.tournament_service.exception;

public class TournamentAlreadyExistsException extends RuntimeException {
    
    public TournamentAlreadyExistsException (String msg) {
        super(msg);
    }

}