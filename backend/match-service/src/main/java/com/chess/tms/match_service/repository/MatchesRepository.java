package com.chess.tms.match_service.repository;

import com.chess.tms.match_service.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchesRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentId(Long tournamentId);
}
