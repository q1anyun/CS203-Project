package com.chess.tms.elo_service.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEloRequestDTO {
    private EloRequestDTO winner;

    public EloRequestDTO loser;
}