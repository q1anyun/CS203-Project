package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.RoundType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    /**
     * Retrieves a distinct list of player counts from all round types.
     * @return List of unique number of players across all round types
     */
    @Query("SELECT DISTINCT r.numberOfPlayers FROM RoundType r")
    public List<Integer> findDistinctNumberOfPlayers();

    /**
     * Finds a round type by its name.
     * @param roundName The name of the round to search for
     * @return Optional containing the round type if found, empty otherwise
     */
    Optional<RoundType> findByRoundName(String roundName);
}