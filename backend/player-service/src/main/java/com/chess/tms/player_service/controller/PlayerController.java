package com.chess.tms.player_service.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;


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
    public ResponseEntity<PlayerDetailsDTO> getCurrentPlayerDetails(@RequestHeader("X-User-PlayerId") String id) {
        Long playerId = Long.parseLong(id);
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(playerId);
        return ResponseEntity.ok(playerDetails);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") long id) {
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
        return ResponseEntity.ok(playerDetails);
    }

    @PutMapping("/currentPlayerById")
    public ResponseEntity<String> updatePlayer(@RequestHeader("X-User-PlayerId") String id,
            @RequestBody UpdatePlayerDetailsDTO updatedPlayerDetails) {
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

    @PostMapping("/list")
    public ResponseEntity<List<PlayerDetailsDTO>> getListOfPlayerDetails(@RequestBody List<Long> playerIds) {
        List<PlayerDetailsDTO> playerDetails = playerService.getListOfPlayerDetails(playerIds);
        return ResponseEntity.ok(playerDetails);
    }

    // for testing
    @PostMapping("/uploadProfile")
    public ResponseEntity<String> uploadProfilePicture(@RequestHeader("X-User-PlayerId") String id,
            @RequestParam("file") MultipartFile file) {
        Long playerId = Long.parseLong(id);
        try {
            playerService.uploadProfilePicture(playerId, file);
        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
        return ResponseEntity.ok("Profile uploaded successfully");
    }

    @GetMapping("profilePicture")
    public ResponseEntity<Resource> getProfilePicture(@RequestHeader("X-User-PlayerId") String id) {
        // Assuming you saved the file as player_{playerId}.jpg
        Long playerId = Long.parseLong(id);
        Path filePath = Paths.get("profile-picture/player_" + playerId + ".jpg"); // Adjust based on your naming convention
        Resource resource = new FileSystemResource(filePath.toFile());
        
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
    
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // Change if using a different format
                .body(resource);
    }

}
