package com.chess.tms.match_service.repository;

import com.chess.tms.match_service.model.SwissBracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SwissBracketRepository extends JpaRepository<SwissBracket, Long> {
    
    // Optional method to find a bracket by tournamentId
    Optional<SwissBracket> findByTournamentId(Long tournamentId);

    // Optional method to find all Swiss Brackets by current round or number of rounds, if needed
    List<SwissBracket> findAllByCurrentRound(int currentRound);
}