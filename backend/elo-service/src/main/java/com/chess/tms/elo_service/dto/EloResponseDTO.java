package com.chess.tms.elo_service.dto;

import java.time.LocalDateTime;

import com.chess.tms.elo_service.enums.Reason;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EloResponseDTO {

    private long playerId;
    
    private int oldElo;

    private int newElo; 

    private Reason changeReason;

    private LocalDateTime createdAt;
}
