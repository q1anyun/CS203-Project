package com.chess.tms.player_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "player_details")
public class PlayerDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "elo_rating")
    private Integer eloRating;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "profile_picture", length = 255)
    private String profilePicture;

    @Column(name = "total_wins")
    private Integer totalWins;

    @Column(name = "total_losses")
    private Integer totalLosses;

    @Column(name = "total_matches")
    private Integer totalMatches;

    @Column(name = "highest_elo")
    private Integer highestElo;

    @Column(name = "lowest_elo")
    private Integer lowestElo;
}
