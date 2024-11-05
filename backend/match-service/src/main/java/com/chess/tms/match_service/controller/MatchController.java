package com.chess.tms.match_service.controller;

import com.chess.tms.match_service.dto.MatchDTO;
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

    // Generate knockout matches for a tournament
    @PostMapping("/knockout/{tournamentId}/{gameTypeId}")
    public ResponseEntity<Long> createKnockoutMatches(@PathVariable ("tournamentId")Long tournamentId, @PathVariable ("gameTypeId")Long gameTypeId) {
        return ResponseEntity.ok(matchService.createKnockoutMatches(tournamentId, gameTypeId, null));
    }

    // Generate swiss matches for a tournament
    @PostMapping("/swiss/{tournamentId}/{gameTypeId}")
    public ResponseEntity<Long> createSwissMatches(@PathVariable ("tournamentId") Long tournamentId, @PathVariable ("gameTypeId") Long gameTypeId) {
        return ResponseEntity.ok(matchService.createSwissMatches(tournamentId, gameTypeId));
    }

    // Get all matches for a tournament
    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<List<MatchDTO>> getMatchesByTournament(@PathVariable ("tournamentId")Long tournamentId) {
        List<MatchDTO> matches = matchService.getMatchesByTournament(tournamentId);
        return ResponseEntity.ok(matches);
    }

    // Get a matches for a tournament
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchDTO> getMatch(@PathVariable ("matchId")Long matchId) {
        MatchDTO match = matchService.getMatch(matchId);
        return ResponseEntity.ok(match);
    }

    // Update match results and advance winner
    @PutMapping("/{matchId}/winner/{winnerId}")
    public ResponseEntity<String> updateMatchResult(
            @PathVariable ("matchId")Long matchId,
            @PathVariable ("winnerId")Long winnerId) {
        return ResponseEntity.ok(matchService.advanceWinner(matchId, winnerId));
    }

    // Recent 5 matches for a player
    @GetMapping("/player/{playerId}/recent")
    public ResponseEntity<List<MatchDTO>> getRecentMatchesByPlayer(@PathVariable ("playerId") Long playerId) {
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
