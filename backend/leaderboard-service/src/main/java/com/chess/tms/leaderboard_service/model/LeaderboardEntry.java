package com.chess.tms.leaderboard_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "leaderboard")
public class LeaderboardEntry {
    
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "player_id", nullable = false, unique = true)
    private long playerId;
    
    @Column(name = "elo_rating", nullable = false)
    private int elo;

    @Column(name = "ranking", nullable = false)
    private int ranking;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}
