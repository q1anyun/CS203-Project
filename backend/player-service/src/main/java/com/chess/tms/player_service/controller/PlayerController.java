package com.chess.tms.player_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.service.PlayerService;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    // @GetMapping("/{id}")
    // public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id")
    // Long id, @RequestHeader("Authorization") String token) {
    // if(tokenValidationService.validateToken(token)){
    // PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
    // return ResponseEntity.ok(playerDetails);
    // }
    // else{
    // throw new UnauthorizedAccessException("Access Denied: You do not have
    // permission to access this resource.");
    // }
    // }

    @GetMapping("/currentPlayerById")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@RequestHeader("X-User-PlayerId") String id) {

        System.out.println("Player Id: " + id);

        Long playerId = Long.parseLong(id);
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(playerId);
        return ResponseEntity.ok(playerDetails);
    }

    @PutMapping("/currentPlayerById")
    public String putMethodName(@PathVariable String id, @RequestBody String entity) {
        //TODO: process PUT request
        
        return entity;
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
