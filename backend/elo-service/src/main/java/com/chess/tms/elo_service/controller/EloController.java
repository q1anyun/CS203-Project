package com.chess.tms.elo_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.elo_service.service.EloService;
import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;

@RestController
@RequestMapping("/api/elo")
public class EloController {

    private EloService eloService;

    //Constructor injection
    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    //Get all Elo histories
    @GetMapping("")
    public ResponseEntity<List<EloResponseDTO>> findAllEloHistory() {
        return ResponseEntity.ok(eloService.findAllByEloHistory());
    }

    //Get all Elo histories by playerId
    @GetMapping("/{playerId}")
    public ResponseEntity<List<EloResponseDTO>> findAllByPlayerId(@PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(eloService.findEloHistoryByPlayerId(playerId));
    }

    //Get all Elo histories by playerId and changeReason
    @GetMapping("/{playerId}/{changeReason}")
    public ResponseEntity<List<EloResponseDTO>> findAllEloHistoriesByPlayerIdAndChangeReason(@PathVariable("playerId") long playerId, @PathVariable("changeReason") String changeReason) {
        return ResponseEntity.ok(eloService.findByPlayerIdAndChangeReason(playerId, changeReason));
    }

    //Delete all Elo histories by playerId
    @DeleteMapping("/deletion/{playerId}") 
    public ResponseEntity<List<EloResponseDTO>> deleteAllPlayerElo(@PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(eloService.deleteByPlayerId(playerId));
    }

    //Update players Elo after completed match
    @PutMapping("/match") 
    public ResponseEntity<String> updatePlayersEloAfterCompletedMatch(@RequestBody MatchEloRequestDTO dto) {
        return ResponseEntity.ok(eloService.updatePlayersEloAfterCompletedMatch(dto));
    }

    //Get current player Elo history for chart
    @GetMapping("/chart/current")
    public ResponseEntity<EloHistoryChartDTO[]> findCurrentPlayerEloHistoryForChart(@RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(playerId));
    }

    //Get player Elo history for chart  
    @GetMapping("/chart/{id}")
    public ResponseEntity<EloHistoryChartDTO[]> findPlayerEloHistoryForChart(@PathVariable("id") long id) {
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(id));
    }
}