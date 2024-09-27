package com.chess.tms.leaderboard_service.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaderboardRequestDTO {

    private long playerId;

    private int elo;

    private int ranking;

    private LocalDateTime lastUpdated;
}