package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.Format;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.SwissBracket;
import com.chess.tms.tournament_service.model.TournamentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDetailsDTO {
    private Long id;

    private Long creatorId;

    private PlayerDetailsDTO winner;

    private String name;

    private Format format;

    private String description;

    private String photo;

    private String country;

    private String locationAddress;

    private Double locationLongitude;

    private Double locationLatitude;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;

    private Integer currentPlayers;

    private Status status;

    private RoundType currentRound;

    private GameType timeControl;

    private TournamentType tournamentType;

    private SwissBracket swissBracket;
}
