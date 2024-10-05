package com.chess.tms.leaderboard_service.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardRequestDTO {

    private long playerId;

    private int elo;

    //private int ranking;

    private LocalDateTime lastUpdated;
}