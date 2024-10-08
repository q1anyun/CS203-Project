package com.chess.tms.leaderboard_service.controller;

import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.leaderboard_service.dto.EloUpdateDTO;
import com.chess.tms.leaderboard_service.dto.LeaderboardDTO;
import com.chess.tms.leaderboard_service.service.LeaderboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;




@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    @Autowired
    public LeaderboardService leaderboardService;

    @GetMapping("")
    public ResponseEntity<List<LeaderboardDTO>> getAllRankings() {
        return ResponseEntity.ok(leaderboardService.findAllRankingsDesc());
    }

    @GetMapping("/desc") 
    public ResponseEntity<List<LeaderboardDTO>> getAllRankingsDesc() {
        return ResponseEntity.ok(leaderboardService.findAllRankingsDesc());
    }

    @GetMapping("/minElo-{minElo}/maxElo-{maxElo}")
    public ResponseEntity<List<LeaderboardDTO>> findByEloBetween(@PathVariable("minElo") int minElo, @PathVariable("maxElo") int maxElo) {
        return ResponseEntity.ok(leaderboardService.findByEloBetween(minElo, maxElo));
    
    }

    @GetMapping("/minRanking-{minRanking}")
    public ResponseEntity<List<LeaderboardDTO>> findAllAboveRanking(@PathVariable("minRanking") int minRank) {
        return ResponseEntity.ok(leaderboardService.findByRankingGreaterThanEqual(minRank));
    }

    @GetMapping("/playerid-{playerId}") 
    public ResponseEntity<LeaderboardDTO> findByPlayerId(@PathVariable long playerId) {
        return ResponseEntity.ok(leaderboardService.findByPlayerId(playerId));
    }

    @PostMapping("")
    public ResponseEntity<LeaderboardDTO> saveEntry(@RequestBody LeaderboardDTO dto) {
        return ResponseEntity.ok(leaderboardService.saveEntry(dto));
    }

    @DeleteMapping("/{playerId}")
    public ResponseEntity<LeaderboardDTO> deleteEntry(@PathVariable long playerId) {
        return ResponseEntity.ok(leaderboardService.deleteEntry(playerId));
    }

    @PutMapping("/updateLeaderboard") 
    public ResponseEntity<LeaderboardDTO> updateEntry(@RequestBody LeaderboardDTO updateDTO) {
        return ResponseEntity.ok(leaderboardService.updateEntry(updateDTO));
    }

    @PutMapping("/updateElo")
    public ResponseEntity<LeaderboardDTO> updateElo(@RequestBody EloUpdateDTO eloUpdateDTO) {
        return ResponseEntity.ok(leaderboardService.updateElo(eloUpdateDTO));
    }

}
    




