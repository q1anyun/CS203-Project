package com.chess.tms.match_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "swiss_bracket")
public class SwissBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tournament_id", nullable = false)
    private Long tournamentId;

    @Column(name = "number_of_rounds", nullable = false)
    private Integer numberOfRounds;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound;
}

