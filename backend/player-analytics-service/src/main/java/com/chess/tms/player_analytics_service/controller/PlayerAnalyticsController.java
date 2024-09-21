package com.chess.tms.player_analytics_service.controller;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_analytics_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_analytics_service.dto.PlayerRegistrationDTO;
import com.chess.tms.player_analytics_service.model.PlayerDetails;
import com.chess.tms.player_analytics_service.service.PlayerAnalyticsService;

@RestController
@RequestMapping("/api/player")
public class PlayerAnalyticsController {

    private final PlayerAnalyticsService playerService;

    public PlayerAnalyticsController(PlayerAnalyticsService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerPlayer(@RequestBody PlayerRegistrationDTO playerRegistrationDTO) {
        return ResponseEntity.ok(playerService.createPlayer(playerRegistrationDTO));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") Long id) {
        Optional<PlayerDetailsDTO> playerDetails = playerService.getPlayerDetailsById(id);
        return playerDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/user")
    public ResponseEntity<Long> getPlayerDetailsbyUserId(@PathVariable("id") Long id) {
        PlayerDetails playerDetails = playerService.getPlayerDetailsByUserId(id);
        return ResponseEntity.ok(playerDetails.getId());
    }

}
