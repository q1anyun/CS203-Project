package com.chess.tms.match_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "game_type")
public class GameType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "time_control_minutes", nullable = false)
    private int timeControlMinutes;
}

