package com.chess.tms.tournament_service.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class TournamentUpdateRequestDTO {
    private long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int minElo;

    private int maxElo;

    private int maxPlayers;

    private int currentPlayers;

    private int timeControl;
}
