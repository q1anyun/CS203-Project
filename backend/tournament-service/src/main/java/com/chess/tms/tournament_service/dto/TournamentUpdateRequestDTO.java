package com.chess.tms.tournament_service.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TournamentUpdateRequestDTO {
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;

    private Integer timeControl;

    private String description;

    private String photo;

    private String format;

    private String country;

    private String locationAddress;

    private Integer locationLatitude;

    private Integer locationLongitude; 
}
