package com.chess.tms.tournament_service.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentUpdateDTO {

    private TournamentDetailsDTO oldTournament;
    private TournamentDetailsDTO newTournament;
}
