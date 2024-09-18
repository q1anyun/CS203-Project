package com.chess.tms.match_service.controller;

import com.chess.tms.match_service.model.Match;
import com.chess.tms.match_service.model.MatchResult;
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
    @PostMapping("/tournament/{tournamentId}/generate")
    public ResponseEntity<String> generateInitialMatches(@PathVariable Long tournamentId) {
        matchService.createInitialMatches(tournamentId);
        return ResponseEntity.ok("Matches created successfully");
    }

    // Get all matches for a tournament
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    // Update match results and advance winner
    @PutMapping("/tournament/{tournamentId}/match/{matchId}/result")
    public ResponseEntity<String> updateMatchResult(
            @PathVariable Long tournamentId,
            @PathVariable Long matchId,
            @RequestBody MatchResult matchResult) {
        matchService.advanceWinner(matchResult);
        return ResponseEntity.ok("Winner advanced to the next round");
    }
}
