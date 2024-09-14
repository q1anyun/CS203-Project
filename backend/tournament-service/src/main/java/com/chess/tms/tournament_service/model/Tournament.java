package com.chess.tms.tournament_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.enums.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tournament")
public class Tournament {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private int minElo;

    @Column
    private int maxElo;

    @Column
    private int maxPlayers;

    @Column
    private int minPlayers;
    
    @Column
    private String location;

    @Column 
    private Status status;

    @Column 
    private Type type;

}
