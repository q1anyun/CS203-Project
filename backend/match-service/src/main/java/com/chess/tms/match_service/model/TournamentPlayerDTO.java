package com.chess.tms.match_service.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentPlayerDTO {
    private Long id;
    private int eloRating;
}
