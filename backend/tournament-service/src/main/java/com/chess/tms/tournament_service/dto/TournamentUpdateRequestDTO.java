package com.chess.tms.tournament_service.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TournamentUpdateRequestDTO {
    private Long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;

    private Integer currentPlayers;

    private Integer timeControl;
}
