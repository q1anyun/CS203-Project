package com.chess.tms.match_service.controller;

import com.chess.tms.match_service.model.Match;
import com.chess.tms.match_service.service.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    @Autowired
    private MatchService matchService;

    // Generate initial matches for a tournament
    @PostMapping("/tournament/{tournamentId}/{gameTypeId}/generate")
    public ResponseEntity<String> generateInitialMatches(@PathVariable Long tournamentId, @PathVariable Long gameTypeId) {
        matchService.createInitialMatches(tournamentId, gameTypeId);
        return ResponseEntity.ok("Matches created successfully");
    }

    // Get all matches for a tournament
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournament(tournamentId);
        System.out.println(matches.get(0).getRoundType());
        return ResponseEntity.ok(matches);
    }

    // Update match results and advance winner
    @PutMapping("/tournament/{matchId}/match/{winnerId}/result")
    public ResponseEntity<String> updateMatchResult(
            @PathVariable Long matchId,
            @PathVariable Long winnerId) {
        matchService.advanceWinner(matchId, winnerId);
        return ResponseEntity.ok("Winner advanced to the next round");
    }
}
