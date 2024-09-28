package com.chess.tms.tournament_service.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.chess.tms.tournament_service.enums.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="tournament")
public class Tournament {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tournamentId;

    @Column(name = "created_by", nullable = false)
    private Long creatorId;

    @Column
    private Long winnerId;

    @Column
    private String name;

    @Column
    private LocalDateTime startDate;

    @Column
    private LocalDateTime endDate;

    @Column
    private Integer minElo;

    @Column
    private Integer maxElo;

    @Column
    private Integer currentPlayers;

    @Column
    private Integer maxPlayers;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "current_round") 
    private RoundType currentRound;

    @ManyToOne
    @JoinColumn(name = "time_control", nullable = false) 
    private GameType timeControl;

    @OneToMany(mappedBy = "tournament",
                cascade = CascadeType.ALL)
    private List<TournamentPlayer> TournamentPlayers;

}
