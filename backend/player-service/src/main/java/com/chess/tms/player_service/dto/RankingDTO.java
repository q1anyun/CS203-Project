package com.chess.tms.player_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RankingDTO {
    private Long playerId; 
    private String firstName;
    private String lastName;
    private Integer eloRating;
    private String profilePicture;
}
