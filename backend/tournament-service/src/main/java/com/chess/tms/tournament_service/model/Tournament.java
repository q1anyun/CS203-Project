package com.chess.tms.tournament_service.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.enums.TimeControl;

import java.time.LocalDateTime;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tournament")
public class Tournament {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long tournamentId;

    @Column(name = "created_by", nullable = false)
    private long creatorId;

    // @Column(name = 'current_round_id') 
    // private List<Long> currentRoundId;

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
    private int totalPlayers;

    // @Column
    // private int currentPlayers;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column
    @Enumerated(EnumType.STRING)
    private TimeControl timeControl;

    @OneToMany(mappedBy = "tournament",
                cascade = CascadeType.ALL)
    private List<TournamentPlayer> TournamentPlayers;

}
