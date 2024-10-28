package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.SwissStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwissStandingRepository extends JpaRepository<SwissStanding, Long> {
    
    // Find all standings for a specific bracket
    List<SwissStanding> findAllByBracketId(Long bracketId);

    // Find a specific standing entry by playerId in a bracket
    Optional<SwissStanding> findByBracketIdAndPlayerId(Long bracketId, Long playerId);

    // Find standings ordered by Bracket Id
    List<SwissStanding> findByBracketId(Long bracketId);

    // Find standings ordered by Bracket Id and Wins
    List<SwissStanding> findByBracketIdOrderByWinsDescLossesAsc(Long bracketId);
}