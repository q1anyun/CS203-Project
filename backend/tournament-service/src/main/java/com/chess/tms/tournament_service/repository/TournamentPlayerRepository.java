package com.chess.tms.tournament_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import java.util.List;
import java.util.Optional;
 

public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayer, Long> {

    public List<TournamentPlayer> findByPlayerId(long playerId);

    public Optional<TournamentPlayer> findByPlayerIdAndTournament(long playerId, Tournament tournament);

    public List<TournamentPlayer> findAllByTournament(Tournament tournament);

    public List<TournamentPlayer> findAll();

    public void deleteByPlayerId(long playerId);

    public List<TournamentPlayer> findAllByPlayerId(long playerId);

    public boolean existsByPlayerIdAndId(long playerId, long tournamentId);

}
