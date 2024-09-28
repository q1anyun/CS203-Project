package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDetailsDTO {
    private Long id;

    private Long creatorId;

    private PlayerDetailsDTO winner;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;

    private Integer currentPlayers;

    private Status status;

    private RoundType currentRound;

    private GameType timeControl;
}
