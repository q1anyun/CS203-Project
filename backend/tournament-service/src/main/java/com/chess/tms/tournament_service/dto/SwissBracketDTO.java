package com.chess.tms.tournament_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SwissBracketDTO {
    private Long id;
    private Integer currentRound;
    private Integer numberOfRounds;
}
