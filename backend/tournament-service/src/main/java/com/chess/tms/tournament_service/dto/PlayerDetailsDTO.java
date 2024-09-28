package com.chess.tms.tournament_service.dto;
import lombok.Data;

@Data
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
    private int winRate;
}