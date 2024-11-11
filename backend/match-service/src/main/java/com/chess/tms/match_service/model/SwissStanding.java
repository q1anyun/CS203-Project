package com.chess.tms.match_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "swiss_standing")
public class SwissStanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bracket_id", nullable = false)
    private SwissBracket bracket;

    @Column(name = "tournament_player_id", nullable = false)
    private Long tournamentPlayerId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "wins", nullable = false)
    private Integer wins;

    @Column(name = "losses", nullable = false)
    private Integer losses;
}

