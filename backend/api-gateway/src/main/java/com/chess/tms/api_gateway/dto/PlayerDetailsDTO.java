package com.chess.tms.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDetailsDTO {

    private Long id;
    private Long userId;
    private int eloRating;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private int totalWins;
    private int totalLosses;
    private int totalMatches;
    private int highestElo;
    private int lowestElo;
}