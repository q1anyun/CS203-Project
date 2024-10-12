package com.chess.tms.match_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchEloRequestDTO {
    private long winner;

    public long loser;
}