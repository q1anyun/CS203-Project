package com.chess.tms.leaderboard_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EloUpdateDTO {

    private long playerId;

    private int newElo;
}
