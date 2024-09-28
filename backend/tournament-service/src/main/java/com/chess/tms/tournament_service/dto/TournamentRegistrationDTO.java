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

    private Long creatorId;

    private String name;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Integer minElo;

    private Integer maxElo;

    private Integer maxPlayers;
    
    private Status status;

    private Integer timeControl;
    
}
