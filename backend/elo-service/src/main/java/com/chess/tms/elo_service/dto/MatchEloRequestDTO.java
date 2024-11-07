package com.chess.tms.elo_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is used to pass in the winner and loser id to the updatePlayersEloAfterCompletedMatch endpoint in EloController
 **/ 
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEloRequestDTO {
    private long winner;

    private long loser;
}