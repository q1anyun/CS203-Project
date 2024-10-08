package com.chess.tms.elo_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.chess.tms.elo_service.dto.EloRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.service.EloService;
import com.chess.tms.elo_service.enums.Reason;

@Controller
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
    public ResponseEntity<EloResponseDTO> saveEloHistory(@RequestBody EloRequestDTO dto) {
        return ResponseEntity.ok(eloService.saveEloHistory(dto));
    }

    // @PutMapping("") 
    // public ResponseEntity<EloDTO> updateByPlayerId(UpdateDTO dto) {
    //     return ResponseEntity.ok(eloService.updateHistory(dto));
    // }
}
