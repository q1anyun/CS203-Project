package com.chess.tms.player_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinLossUpdateDTO {
    long playerId;
    int newElo;
    boolean isWinner;
}
