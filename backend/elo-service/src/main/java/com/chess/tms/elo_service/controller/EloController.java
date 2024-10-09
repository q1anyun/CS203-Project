package com.chess.tms.elo_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.EloRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.service.EloService;
import com.chess.tms.elo_service.enums.Reason;

@RestController
@RequestMapping("/api/elo")
public class EloController {

    private EloService eloService;


    public EloController(EloService eloService) {
        this.eloService = eloService;
    }

    @GetMapping("")
    public ResponseEntity<List<EloResponseDTO>> findAllEloHistory() {
        return ResponseEntity.ok(eloService.findAllByEloHistory());
    }

    @GetMapping("/{playerId}")
    public ResponseEntity<List<EloResponseDTO>> findAllByPlayerId(@PathVariable long playerId) {
        return ResponseEntity.ok(eloService.findEloHistoryByPlayerId(playerId));
    }

    @GetMapping("/{playerId}/{changeReason}")
    public ResponseEntity<List<EloResponseDTO>> findAllByPlayerId(@PathVariable long playerId, @PathVariable String changeReason) {
        return ResponseEntity.ok(eloService.findByPlayerIdAndChangeReason(playerId, changeReason));
    }


    @DeleteMapping("/delete-{playerId}") 
    public ResponseEntity<List<EloResponseDTO>> deletePlayerElo(@PathVariable long playerId) {
        return ResponseEntity.ok(eloService.deleteByPlayerId(playerId));
    }

    @PostMapping("")
    public ResponseEntity<EloResponseDTO> saveEloHistory(@RequestBody EloHistoryRequestDTO dto) {
        return ResponseEntity.ok(eloService.saveEloHistory(dto));
    }

    @PutMapping("/match") 
    public void updateMatchPlayersElo(@RequestBody MatchEloRequestDTO dto) {
        eloService.updateMatchPlayersElo(dto);
    }

    @GetMapping("/chart/current")
    public ResponseEntity<List<EloHistoryChartDTO>> findCurrentPlayerEloHistoryForChart(@RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(playerId));
    }

    @GetMapping("/chart/{id}")
    public ResponseEntity<List<EloHistoryChartDTO>> findPlayerEloHistoryForChart(@PathVariable long id) {
        return ResponseEntity.ok(eloService.findPlayerEloHistoryForChart(id));
    }
}
