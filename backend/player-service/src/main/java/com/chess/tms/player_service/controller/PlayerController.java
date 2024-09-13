package com.chess.tms.player_service.controller;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.PlayerRegistrationDTO;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.service.PlayerService;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<PlayerDetails> registerPlayer(@RequestBody PlayerRegistrationDTO playerRegistrationDTO) {
        PlayerDetails playerDetails = playerService.createPlayer(playerRegistrationDTO);
        return ResponseEntity.ok(playerDetails);
    }

        @GetMapping("/{id}/details")
    public ResponseEntity<PlayerDetailsDTO> getUserDetails(@PathVariable("id") Long id) {
        Optional<PlayerDetailsDTO> playerDetails = playerService.getPlayerDetailsById(id);
        return playerDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
