package com.chess.tms.match_service.repository;

import com.chess.tms.match_service.model.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    Optional<RoundType> findByNumberOfPlayers(int numberOfPlayers);

    @Query("SELECT r FROM RoundType r WHERE r.numberOfPlayers = (SELECT MIN(r2.numberOfPlayers) FROM Match m JOIN m.roundType r2 WHERE m.status = 'COMPLETED' AND m.tournamentId = :tournamentId)")
    Optional<RoundType> findMostRecentCompletedRound(@Param("tournamentId") Long tournamentId);
}
