package com.chess.tms.elo_service.exception.dto;

import com.chess.tms.elo_service.enums.Reason;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EloHistoryRequestDTO {
    private long playerId;

    private int newElo; 
    
    private Reason changeReason;
}
