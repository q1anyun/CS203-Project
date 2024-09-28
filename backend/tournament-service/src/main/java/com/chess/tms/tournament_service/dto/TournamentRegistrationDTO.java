package com.chess.tms.tournament_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.chess.tms.tournament_service.enums.Status;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentRegistrationDTO {

    private long creatorId;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private int minElo;

    private int maxElo;

    private int maxPlayers;

    private int currentPlayers;

    private Status status;

    private int timeControl;
    
}
