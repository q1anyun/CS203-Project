package com.chess.tms.match_service.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private String country;
}