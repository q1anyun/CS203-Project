package com.chess.tms.tournament_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tournament_type")
public class TournamentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_name", nullable = false, length = 50)
    private String typeName; 
}

