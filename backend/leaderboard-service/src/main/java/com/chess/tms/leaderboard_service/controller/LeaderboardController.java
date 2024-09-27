package com.chess.tms.leaderboard_service.controller;

import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.leaderboard_service.dto.LeaderboardDTO;
import com.chess.tms.leaderboard_service.service.LeaderboardService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

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
        return ResponseEntity.ok(leaderboardService.findAllRankings());
    }

    @GetMapping("minElo-{minElo}/maxElo-{maxElo}")
    public ResponseEntity<List<LeaderboardDTO>> findByEloBetween(@RequestParam("minElo") int minElo, @RequestParam("maxElo") int maxElo) {
        return ResponseEntity.ok(leaderboardService.findByEloBetween(minElo, maxElo));
    
    }

    @GetMapping("minRanking-{minRanking}")
    public ResponseEntity<List<LeaderboardDTO>> findAllAboveRanking(int minRank) {
        return ResponseEntity.ok(leaderboardService.findByRankingGreaterThanEqual(minRank));
    }

    @PostMapping("")
    public ResponseEntity<LeaderboardDTO> saveEntry(@RequestBody LeaderboardDTO dto) {
        return ResponseEntity.ok(leaderboardService.saveEntry(dto));
    }

    @DeleteMapping("id")
    public ResponseEntity<LeaderboardDTO> deleteEntry(long playerId) {
        return ResponseEntity.ok(leaderboardService.deleteEntry(playerId));
    }

    @PutMapping("updateLeaderboard/{entryId}") 
    public ResponseEntity<LeaderboardDTO> updateEntry(LeaderboardDTO updateDTO) {
        return ResponseEntity.ok(leaderboardService.updateEntry(updateDTO));
    }


}
    




