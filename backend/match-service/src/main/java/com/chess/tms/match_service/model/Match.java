package com.chess.tms.match_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    public enum MatchStatus {
        ONGOING,
        PENDING,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "player1_id")
    private Long player1Id;

    @Column(name = "player2_id")
    private Long player2Id;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "loser_id")
    private Long loserId;

    @Column(name = "swiss_round_number")
    private Integer swissRoundNumber;

    @ManyToOne
    @JoinColumn(name = "round_type_id", referencedColumnName = "id", nullable = false)
    private RoundType roundType;

    @ManyToOne
    @JoinColumn(name = "game_type_id", referencedColumnName = "id", nullable = false)
    private GameType gameType;

    @Column(name = "next_match_id")
    private Long nextMatchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MatchStatus status;

    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
