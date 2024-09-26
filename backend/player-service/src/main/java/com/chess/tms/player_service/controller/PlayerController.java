package com.chess.tms.player_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_service.dto.MatchResponseDTO;
import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.service.PlayerService;


@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("/currentPlayerById")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@RequestHeader("X-User-PlayerId") String id) {

        System.out.println("Player Id: " + id);

        Long playerId = Long.parseLong(id);
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(playerId);
        return ResponseEntity.ok(playerDetails);
    }

    @PutMapping("/currentPlayerById")
    public ResponseEntity<String> updatePlayer(@RequestHeader("X-User-PlayerId") String id, @RequestBody UpdatePlayerDetailsDTO updatedPlayerDetails) {
        Long playerId = Long.parseLong(id);
        playerService.updatePlayer(playerId, updatedPlayerDetails);
        return ResponseEntity.ok("Suceessfully updated player");
    }
    
    @GetMapping("/recentMatches")
    public ResponseEntity<List<MatchResponseDTO>> getRecentMatches(@RequestHeader("X-User-PlayerId") String id) {
        Long playerId = Long.parseLong(id);
        List<MatchResponseDTO> recentMatches = playerService.getRecentMatches(playerId);
        return ResponseEntity.ok(recentMatches);
    }

    // For Testing without Token Access
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetailsTest(@PathVariable("id") Long id) {
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
        return ResponseEntity.ok(playerDetails);
    }

    // For Testing without Token Access
    @GetMapping("/user/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetailsbyUserIdTest(@PathVariable("id") Long id) {
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsByUserId(id);
        return ResponseEntity.ok(playerDetails);
    }
}
