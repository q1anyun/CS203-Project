package com.chess.tms.leaderboard_service.exception;

public class EntryAlreadyExistsException extends RuntimeException{
    
    public EntryAlreadyExistsException(String msg) {
        super(msg);
    }
    
 }
