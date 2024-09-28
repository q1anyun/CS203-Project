package com.chess.tms.match_service.controller;

import com.chess.tms.match_service.dto.MatchDTO;
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
    @PostMapping("/{tournamentId}/{gameTypeId}/generate")
    public ResponseEntity<Long> generateInitialMatches(@PathVariable Long tournamentId, @PathVariable Long gameTypeId) {
        return ResponseEntity.ok(matchService.createInitialMatches(tournamentId, gameTypeId));
    }

    // Get all matches for a tournament
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<Match>> getMatchesByTournament(@PathVariable Long tournamentId) {
        List<Match> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    // Get a matches for a tournament
    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getMatch(@PathVariable Long matchId) {
        Match match = matchService.getMatch(matchId);
        return ResponseEntity.ok(match);
    }

    // Update match results and advance winner
    @PutMapping("/{matchId}/winner/{winnerId}")
    public ResponseEntity<String> updateMatchResult(
            @PathVariable Long matchId,
            @PathVariable Long winnerId) {
        return ResponseEntity.ok(matchService.advanceWinner(matchId, winnerId));
    }

    // Recent 5 matches for a player
    @GetMapping("/player/{playerId}/recent")
    public ResponseEntity<List<MatchDTO>> getRecentMatchesByPlayer(@PathVariable Long playerId) {
        List<MatchDTO> recentMatches = matchService.getRecentMatchesByPlayerId(playerId);
        return ResponseEntity.ok(recentMatches);
    }

     // Recent 5 matches for current player
     @GetMapping("/player/current/recent")
     public ResponseEntity<List<MatchDTO>> getRecentMatchesByPlayer(@RequestHeader("X-User-PlayerId") String id) {
        Long playerId = Long.parseLong(id); 
        List<MatchDTO> recentMatches = matchService.getRecentMatchesByPlayerId(playerId);
         return ResponseEntity.ok(recentMatches);
     }

}
