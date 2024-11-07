package com.chess.tms.elo_service.model;

import java.time.LocalDateTime;

import com.chess.tms.elo_service.enums.Reason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class represents an entry in the elo_history table
 */ 
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name="elo_history")
public class EloHistory{
        
    @Column(name = "id", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "player_id", nullable = false)
    private long playerId;
    
    @Column(name = "old_elo", nullable = false)
    private int oldElo;

    @Column(name = "new_elo", nullable = false)
    private int newElo;

    @Column(name = "change_reason", nullable = false)
    @Enumerated(EnumType.STRING)
    private Reason changeReason;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;
}