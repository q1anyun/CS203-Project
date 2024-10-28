package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.SwissBracket;
import com.chess.tms.tournament_service.model.Tournament;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SwissBracketRepository extends JpaRepository<SwissBracket, Long> {
    
    // Optional method to find a bracket by tournament
    Optional<SwissBracket> findByTournament(Tournament tournament);

    // Optional method to find all Swiss Brackets by current round or number of rounds, if needed
    List<SwissBracket> findAllByCurrentRound(int currentRound);
}