package com.chess.tms.player_service.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;


import com.chess.tms.player_service.dto.MatchResponseDTO;
import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.RankingDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.dto.WinLossUpdateDTO;
import com.chess.tms.player_service.service.PlayerService;

@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping("")
    public ResponseEntity<List<PlayerDetailsDTO>> getAllPlayers() {
        List<PlayerDetailsDTO> allPlayers = playerService.getAllPlayers();
        return ResponseEntity.ok(allPlayers);
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
        return ResponseEntity.ok("Successfully updated player");
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

    @GetMapping("/getTop100Players")
    public ResponseEntity<List<RankingDTO>> findTop100Players() {
        return ResponseEntity.ok(playerService.findTop100Players());
    }

    @GetMapping("/elo/{id}")
    public ResponseEntity<Integer> getPlayerElo(@PathVariable("id") long id) {
        Integer elo = playerService.getPlayerElo(id);
        return ResponseEntity.ok(elo);
    }

    @PutMapping("/updateWinLossElo")
    public void updateWinLossElo(@RequestBody WinLossUpdateDTO dto) {
        System.out.println("Updating player "+ dto.getPlayerId()+" elo:" + dto.getNewElo());
        playerService.updateWinLossElo(dto);
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

    
    // for testing
    @PostMapping("/uploadProfile/{id}")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable("id") long id,
            @RequestParam("file") MultipartFile file) {
        try {
            playerService.uploadProfilePicture(id, file);
        } catch (IOException e) {
            System.out.println("File upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File upload failed");
        }
        return ResponseEntity.ok("Profile uploaded successfully");
    }

  
     @GetMapping("/photo/{id}") // Accept filename as a path variable
    public ResponseEntity<InputStreamResource> getPhoto(@PathVariable("id") long id) {
        try {
            String filename = "player_" + id; 
            byte[] photoData = playerService.getProfilePicture(filename); // Retrieve the photo data
            // Check if photoData is null
            if (photoData == null || photoData.length == 0) {
                System.out.println("Photo data is null or empty for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if no photo found
            }
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(determineMediaType(filename)); // Set the correct media type
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(photoData))); // Return as InputStreamResource
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if photo not found
        }
    }

    @GetMapping("/photo") // Accept filename as a path variable
    public ResponseEntity<InputStreamResource> getPhoto(@RequestHeader("X-User-PlayerId") String id) {
        try {
            String filename = "player_" + id; 
            byte[] photoData = playerService.getProfilePicture(filename); // Retrieve the photo data
            // Check if photoData is null
            if (photoData == null || photoData.length == 0) {
                System.out.println("Photo data is null or empty for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if no photo found
            }
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(determineMediaType(filename)); // Set the correct media type
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(photoData))); // Return as InputStreamResource
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if photo not found
        }
    }
    // Method to determine media type based on filename
    private MediaType determineMediaType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        // Default media type
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @GetMapping("/getRanking")
    public ResponseEntity<Integer> getRankingForCurrentPlayer(@RequestHeader("X-User-PlayerId") String id) {
        return ResponseEntity.ok(playerService.getRankingForCurrentPlayer(Long.parseLong(id)));
    }

}
