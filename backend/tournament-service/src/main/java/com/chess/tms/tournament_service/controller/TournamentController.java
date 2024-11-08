package com.chess.tms.tournament_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.service.TournamentService;

import jakarta.validation.Valid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;


    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Tournament service is up and running");
    }

    @PostMapping("")
    public ResponseEntity<String> createTournament(@Valid @RequestBody TournamentRegistrationDTO dto,
            @RequestHeader("X-User-Id") String id) {
        return ResponseEntity.ok(tournamentService.createTournament(dto, Long.parseLong(id)));
    }

    @PostMapping("/start/{tournamentId}")
    public ResponseEntity<String> startTournament(@PathVariable("tournamentId") long tournamentId) {
        return ResponseEntity.ok(tournamentService.startTournament(tournamentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDetailsDTO> getTournament(@PathVariable("id") long id) {
        return ResponseEntity.ok(tournamentService.getTournamentDetailsById(id));
    }

    @GetMapping("")
    public ResponseEntity<List<TournamentDetailsDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<TournamentDetailsDTO>> getRecommendedTournaments(
            @RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(tournamentService.getRecommendedTournaments(playerId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTournament(@PathVariable("id") long id,
            @RequestBody TournamentUpdateRequestDTO updatedDTO) {
        tournamentService.updateTournament(id, updatedDTO);
        return ResponseEntity.ok("Tournament updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTournament(@PathVariable("id") long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.ok("Successfully deleted tournament");
    }

    @PutMapping("/{id}/round/{round_type}")
    public ResponseEntity<String> updateCurrentRoundForTournament(@PathVariable("id") long id,
            @PathVariable("round_type") long roundType) {
        tournamentService.updateCurrentRoundForTournament(id, roundType);
        return ResponseEntity.ok("Current round updated to " + roundType);
    }

    @PutMapping("/{id}/winner/{winnerId}")
    public ResponseEntity<String> completeTournament(@PathVariable("id") long id,
            @PathVariable("winnerId") long winnerId) {
        String response = tournamentService.completeTournament(id, winnerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registered/{playerId}")
    public ResponseEntity<List<TournamentDetailsDTO>> getRegisteredTournaments(
            @PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(tournamentService.getRegisteredTournaments(playerId));
    }

    @GetMapping("/registered/current")
    public ResponseEntity<List<TournamentDetailsDTO>> getRegisteredTournaments(
            @RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(tournamentService.getRegisteredTournaments(playerId));
    }

    @GetMapping("/live/current")
    public ResponseEntity<List<TournamentDetailsDTO>> getLiveTournaments(@RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(tournamentService.getLiveTournaments(playerId));
    }

    @GetMapping("/live/{playerId}")
    public ResponseEntity<List<TournamentDetailsDTO>> getLiveTournaments(@PathVariable("playerId") long playerId) {
        return ResponseEntity.ok(tournamentService.getLiveTournaments(playerId));
    }

    @PostMapping("/uploadTournamentImage/{tournamentId}")
    public ResponseEntity<String> uploadTournamentImage(
            @PathVariable("tournamentId") long tournamentId,
            @RequestPart("file") MultipartFile file) {
        try {
            tournamentService.uploadTournamentImage(tournamentId, file);
            return ResponseEntity.ok("Image uploaded successfully.");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to upload image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getTournamentImage/{tournamentId}")
    public ResponseEntity<InputStreamResource> getTournamentImage(@PathVariable("tournamentId") String tournamentIdString) {
        System.out.println("Received ID: " + tournamentIdString); // Log the received ID
        try {
            long tournamentId = Long.parseLong(tournamentIdString);
            String filename = "tournament_" + tournamentId; // Construct the filename
            byte[] photoData = tournamentService.getTournamentImage(filename); // Fetch the photo data
    
            // Check if photoData is null
            if (photoData == null || photoData.length == 0) {
                System.out.println("Photo data is null or empty for ID: " + tournamentId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Return 404 if no photo found
            }
    
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(determineMediaType(filename)); // Set the correct media type
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(photoData))); // Return as InputStreamResource
        } catch (IOException e) {
            System.out.println("IOException occurred: " + e.getMessage()); // Log the exception message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 on IO error
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage()); // Log unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Return 500 for other exceptions
        }
    }

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

}
