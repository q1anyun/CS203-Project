package com.chess.tms.player_service.dto;
import lombok.Data;

@Data
public class MatchDTO {
    private TournamentDTO tournament;
    private Long winnerId;
    private Long loserId;
    private RoundTypeDTO roundType;
    private GameTypeDTO gameType;
}
