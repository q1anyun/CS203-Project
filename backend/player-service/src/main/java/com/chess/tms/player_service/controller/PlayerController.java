package com.chess.tms.player_service.controller;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.service.PlayerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
    //     if(tokenValidationService.validateToken(token)){
    //         PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
    //         return ResponseEntity.ok(playerDetails);
    //     }
    //     else{
    //        throw new UnauthorizedAccessException("Access Denied: You do not have permission to access this resource.");
    //     }
    // }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") Long id, @RequestHeader("X-User-PlayerId") String playerId) {

    System.out.println("Player Id: " + playerId);

            PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
            return ResponseEntity.ok(playerDetails);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetailsbyUserId(@PathVariable("id") Long id) {
            PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsByUserId(id);
            return ResponseEntity.ok(playerDetails);
    }
}
