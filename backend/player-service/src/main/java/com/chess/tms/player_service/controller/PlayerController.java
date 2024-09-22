package com.chess.tms.player_service.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.PlayerRegistrationDTO;
import com.chess.tms.player_service.exception.UnauthorizedAccessException;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.service.PlayerService;
import com.chess.tms.player_service.service.TokenValidationService;

@RestController
@RequestMapping("/api/player")
public class PlayerController {

    private final PlayerService playerService;
    private final TokenValidationService tokenValidationService;

    public PlayerController(PlayerService playerService, TokenValidationService tokenValidationService) {
        this.playerService = playerService;
        this.tokenValidationService = tokenValidationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        if(tokenValidationService.validateToken(token)){
            PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
            return ResponseEntity.ok(playerDetails);
        }
        else{
           throw new UnauthorizedAccessException("Access Denied: You do not have permission to access this resource.");
        }
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetailsbyUserId(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        if(tokenValidationService.validateToken(token)){
            PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsByUserId(id);
            return ResponseEntity.ok(playerDetails);
        }
        else{
           throw new UnauthorizedAccessException("Access Denied: You do not have permission to access this resource.");
        }
    }

}
