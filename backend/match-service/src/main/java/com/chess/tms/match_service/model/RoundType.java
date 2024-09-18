package com.chess.tms.match_service.model;

import jakarta.persistence.*;

@Entity
@Table(name = "round_type")
public class RoundType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_name", nullable = false, length = 50)
    private String roundName; 

    @Column(name = "number_of_players")
    private int numberOfPlayers;
}

