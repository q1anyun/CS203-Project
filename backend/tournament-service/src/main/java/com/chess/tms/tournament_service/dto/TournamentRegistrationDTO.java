package com.chess.tms.tournament_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRegistrationDTO {
    @NotNull(message = "Missing name")
    private String name;

    @NotNull(message = "Missing start date")
    private LocalDateTime startDate;

    @NotNull(message = "Missing end date")
    private LocalDateTime endDate;

    @NotNull(message = "Missing minimum elo")
    private Integer minElo;

    @NotNull(message = "Missing maximum elo")
    private Integer maxElo;

    @NotNull(message = "Missing maximum players allowed")
    private Integer maxPlayers;

    @NotNull(message = "Missing game type")
    private Integer timeControl;
    
}
