package com.chess.tms.match_service.dto;

import lombok.Data;

@Data
public class TournamentPlayerDTO {
    Long id;
    Long tournamentId;
    Long playerId;
}
