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

/**
 * This class receives REST requests forwarded from gateway service and 
 * maps them to the corresponding endpoint 
 */
@RestController
@RequestMapping("/api/elo")
public class EloController {

    private final EloService eloService;

    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    /**
     * Retrieves complete Elo history for all players
     */
    @GetMapping("")
    public ResponseEntity<List<EloResponseDTO>> findAllEloHistory() {
        return ResponseEntity.ok(eloService.findAllByEloHistory());
    }

    /**
     * Saves a new Elo history entry
     */
    @PostMapping("")
    public ResponseEntity<EloResponseDTO> saveEloHistory(@RequestBody EloHistoryRequestDTO dto) {
        return ResponseEntity.ok(eloService.saveEloHistory(dto));
    }

    /**
     * Retrieves Elo history for a specific player
     */
    @GetMapping("/{playerId}")
    public ResponseEntity<List<EloResponseDTO>> findAllByPlayerId(@PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(eloService.findEloHistoryByPlayerId(playerId));
    }

    /**
     * Retrieves filtered Elo history for a player based on change reason
     */
    @GetMapping("/{playerId}/{changeReason}")
    public ResponseEntity<List<EloResponseDTO>> findAllByPlayerIdAndChangeReason(
            @PathVariable("playerId") long playerId,
            @PathVariable("changeReason") String changeReason) {
        return ResponseEntity.ok(eloService.findByPlayerIdAndChangeReason(playerId, changeReason));
    }

    /**
     * Retrieves chart data for the current player's Elo history
     */
    @GetMapping("/chart/current")
    public ResponseEntity<List<EloHistoryChartDTO>> findCurrentPlayerEloHistoryForChart(
            @RequestHeader("X-User-PlayerId") String id) {
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(Long.parseLong(id)));
    }

    /**
     * Retrieves chart data for a specific player's Elo history
     */
    @GetMapping("/chart/{id}")
    public ResponseEntity<EloHistoryChartDTO[]> findPlayerEloHistoryForChart(@PathVariable("id") long id) {
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(id));
    }

    /**
     * Updates Elo ratings for players after a match
     */
    @PutMapping("/match")
    public ResponseEntity<Void> updateMatchPlayersElo(@RequestBody MatchEloRequestDTO dto) {
        eloService.updateMatchPlayersElo(dto);
        return ResponseEntity.ok().build();
    }

    /**
     * Deletes all Elo history for a specific player
     */
    @DeleteMapping("/delete/{playerId}")
    public ResponseEntity<List<EloResponseDTO>> deletePlayerElo(@PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(eloService.deleteByPlayerId(playerId));
    }
}