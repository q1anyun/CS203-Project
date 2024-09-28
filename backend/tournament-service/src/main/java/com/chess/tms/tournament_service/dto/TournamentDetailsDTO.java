package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.model.GameType;

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

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;

    private Integer currentPlayers;

    private Status status;

    private GameType timeControl;
}
