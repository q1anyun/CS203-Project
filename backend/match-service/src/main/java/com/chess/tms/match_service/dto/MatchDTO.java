package com.chess.tms.match_service.dto;
import com.chess.tms.match_service.model.GameType;
import com.chess.tms.match_service.model.RoundType;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MatchDTO {
    private Long id;
    private TournamentDTO tournament;
    private PlayerDetailsDTO winnerId;
    private PlayerDetailsDTO loserId;
    private RoundType roundType;
    private GameType gameType;
    private LocalDateTime date;
}
