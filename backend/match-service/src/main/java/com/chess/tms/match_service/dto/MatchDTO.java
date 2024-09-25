package com.chess.tms.match_service.dto;
import com.chess.tms.match_service.model.GameType;
import com.chess.tms.match_service.model.RoundType;

import lombok.Data;

@Data
public class MatchDTO {
    private TournamentDTO tournament;
    private Long winnerId;
    private Long loserId;
    private RoundType roundType;
    private GameType gameType;
}
