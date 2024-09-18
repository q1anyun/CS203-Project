package com.chess.tms.match_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
public class Match {

    public enum MatchStatus {
        PENDING,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "player1_id", nullable = false)
    private Long player1Id;

    @Column(name = "player2_id", nullable = false)
    private Long player2Id;

    @ManyToOne
    @JoinColumn(name = "round_type_id", referencedColumnName = "id")
    private RoundType roundType;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    @Column(name = "next_match_id")
    private Long nextMatchId;
}
