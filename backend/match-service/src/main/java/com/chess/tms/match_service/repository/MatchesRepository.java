package com.chess.tms.match_service.repository;

import com.chess.tms.match_service.model.Match;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchesRepository extends JpaRepository<Match, Long> {

    List<Match> findByTournamentId(Long tournamentId);

    @Query("SELECT m FROM Match m WHERE m.tournamentId = :tournamentId AND m.roundType.id = :roundTypeId AND m.status = 'COMPLETED'")
    List<Match> findCompletedMatchesByRoundType(@Param("tournamentId") Long tournamentId, @Param("roundTypeId") Long roundTypeId);

    List<Match> findTop5ByPlayer1IdOrPlayer2IdOrderByUpdatedAtDesc(Long player1Id, Long player2Id);

    List<Match> findByTournamentIdAndRoundTypeId(Long tournamentId, Long roundTypeId);

    List<Match> findByTournamentIdAndSwissRoundNumber(Long tournamentId, Integer swissRoundNumber);

    @Query("SELECT m FROM Match m WHERE m.tournamentId = :tournamentId AND (m.player1Id = :playerId OR m.player2Id = :playerId)")
    List<Match> findByPlayerIdInTournament(@Param("playerId") Long playerId, @Param("tournamentId") Long tournamentId);
}
