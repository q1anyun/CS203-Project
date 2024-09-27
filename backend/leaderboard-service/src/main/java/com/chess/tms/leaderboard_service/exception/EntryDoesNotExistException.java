package com.chess.tms.leaderboard_service.exception;

public class EntryDoesNotExistException extends RuntimeException{
    
    public EntryDoesNotExistException(String msg) {
        super(msg);
    }
    
 }
