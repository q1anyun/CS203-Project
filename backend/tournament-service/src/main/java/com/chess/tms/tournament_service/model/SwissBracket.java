package com.chess.tms.tournament_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "swiss_bracket")
public class SwissBracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(mappedBy = "swissBracket")
    private Tournament tournament;

    @Column(name = "number_of_rounds", nullable = false)
    private Integer numberOfRounds;

    @Column(name = "current_round", nullable = false)
    private Integer currentRound;
}

