package com.chess.tms.player_service.dto;

import jakarta.persistence.Column;
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
    private String country;
}