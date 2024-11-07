package com.chess.tms.elo_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is used to send details of a player to update the elo of the corresponding
 * player in Player Service after a win/loss.
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WinLossUpdateDTO {
    long playerId;
    int newElo;
    boolean isWinner;
}
