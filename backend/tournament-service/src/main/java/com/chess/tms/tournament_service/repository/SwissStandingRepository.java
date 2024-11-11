package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.SwissStanding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwissStandingRepository extends JpaRepository<SwissStanding, Long> {
    /**
     * Retrieves all standings for a given bracket.
     * @param bracketId The ID of the bracket
     * @return List of all standings in the bracket, unordered
     */
    List<SwissStanding> findAllByBracketId(Long bracketId);

    /**
     * Finds a player's standing entry within a specific bracket.
     * @param bracketId The ID of the bracket
     * @param playerId The ID of the player
     * @return Optional containing the standing if found, empty otherwise
     */
    Optional<SwissStanding> findByBracketIdAndPlayerId(Long bracketId, Long playerId);

    /**
     * Retrieves standings for a specific bracket.
     * Note: Consider removing this method as it duplicates findAllByBracketId
     * @param bracketId The ID of the bracket
     * @return List of standings for the bracket
     * @deprecated Use findAllByBracketId instead
     */
    List<SwissStanding> findByBracketId(Long bracketId);

    /**
     * Retrieves standings for a bracket, ordered by wins (descending) and losses (ascending).
     * This ordering places players with more wins and fewer losses at the top.
     * @param bracketId The ID of the bracket
     * @return Ordered list of standings for the bracket
     */
    List<SwissStanding> findByBracketIdOrderByWinsDescLossesAsc(Long bracketId);
}