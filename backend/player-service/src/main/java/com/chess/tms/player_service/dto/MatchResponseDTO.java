package com.chess.tms.player_service.dto;
import com.chess.tms.player_service.model.PlayerDetails;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MatchResponseDTO {
    private TournamentDTO tournament;
    private PlayerDetails winner;
    private PlayerDetails loser;
    private RoundTypeDTO roundType;
    private GameTypeDTO gameType;
    private LocalDateTime date;
}
