package com.chess.tms.match_service.model;

import jakarta.persistence.*;

public class MatchResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

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
