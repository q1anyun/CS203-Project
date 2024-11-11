package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.SwissBracket;
import com.chess.tms.tournament_service.model.Tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SwissBracketRepository extends JpaRepository<SwissBracket, Long> {
    /**
     * Finds a Swiss bracket associated with a specific tournament.
     *
     * @param tournament the tournament entity to search by
     * @return an Optional containing the SwissBracket if found, empty Optional otherwise
     */
    Optional<SwissBracket> findByTournament(Tournament tournament);

    /**
     * Retrieves all Swiss brackets that are currently in a specific round.
     *
     * @param currentRound the round number to search for
     * @return a List of SwissBracket entities in the specified round
     */
    List<SwissBracket> findAllByCurrentRound(int currentRound);
}