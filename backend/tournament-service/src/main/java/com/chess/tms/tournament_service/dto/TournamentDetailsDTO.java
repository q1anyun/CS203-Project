package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.enums.Type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDetailsDTO {

    private long id;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int minElo;

    private int maxElo;

    private int maxPlayers;

    private int minPlayers;

    private String location;

    private Status status;

    private Type type;
}