package com.chess.tms.player_service.dto;
import lombok.Data;

@Data
public class PlayerDetailsDTO {
    private Long id;
    private Long userId;
    private Integer eloRating;
    private String firstName;
    private String lastName;
    private String profilePicture;
    private Integer totalWins;
    private Integer totalLosses;
    private Integer totalMatches;
    private Integer highestElo;
    private Integer lowestElo;
    private String country;
}