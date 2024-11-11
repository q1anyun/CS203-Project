package com.chess.tms.player_service.controller;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;

import com.chess.tms.player_service.dto.MatchResponseDTO;
import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.RankingDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.dto.WinLossUpdateDTO;
import com.chess.tms.player_service.exception.UserNotFoundException;
import com.chess.tms.player_service.service.PlayerService;

@RestController
@RequestMapping("/api/player")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Retrieves a list of all player details.
     * 
     * This endpoint fetches and returns all player details in the system.
     * It is intended to be used for listing all players for administrative or
     * viewing purposes.
     * 
     * @return ResponseEntity containing a list of PlayerDetailsDTO objects
     *         representing all players.
     */
    @GetMapping("")
    public ResponseEntity<List<PlayerDetailsDTO>> getAllPlayers() {
        List<PlayerDetailsDTO> allPlayers = playerService.getAllPlayers();
        return ResponseEntity.ok(allPlayers);
    }

    /**
     * Retrieves the details of the current player by their ID.  
     * 
     * This endpoint fetches and returns the details of the player specified by the
     * "X-User-PlayerId" header.
     * It is intended for retrieving the information of a currently logged-in or
     * authenticated player.
     * 
     * @param id The ID of the player, passed as a custom request header
     *           ("X-User-PlayerId").
     * @return ResponseEntity containing the PlayerDetailsDTO of the specified
     *         player.
     * @throws UserNotFoundException if no player is found with the provided ID.
     */
    @GetMapping("/currentPlayerById")
    public ResponseEntity<PlayerDetailsDTO> getCurrentPlayerDetails(@RequestHeader("X-User-PlayerId") String id) {
        Long playerId = Long.parseLong(id);
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(playerId);
        return ResponseEntity.ok(playerDetails);
    }

    /**
     * Retrieves the details of a player by their ID.
     * 
     * This endpoint fetches and returns the details of the player specified by the
     * ID in the URL path.
     * It is intended for retrieving the information of a player based on their
     * unique ID.
     * 
     * @param id The ID of the player, passed as a path variable in the URL.
     * @return ResponseEntity containing the PlayerDetailsDTO of the specified
     *         player.
     * @throws UserNotFoundException if no player is found with the provided ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDetailsDTO> getPlayerDetails(@PathVariable("id") long id) {
        PlayerDetailsDTO playerDetails = playerService.getPlayerDetailsById(id);
        return ResponseEntity.ok(playerDetails);
    }

    /**
     * Endpoint to update the details of the current player.
     * 
     * This method receives the player's ID via the request header, and the updated
     * player details in the request body.
     * It calls the service to update the player in the database and returns a
     * message indicating whether the update was successful.
     * 
     * @param id                   The player ID, passed in the request header
     *                             "X-User-PlayerId".
     * @param updatedPlayerDetails The updated player details provided in the
     *                             request body.
     * @return A ResponseEntity containing a string message indicating the outcome
     *         of the update operation.
     */
    @PutMapping("/currentPlayerById")
    public ResponseEntity<String> updatePlayer(@RequestHeader("X-User-PlayerId") String id,
            @RequestBody UpdatePlayerDetailsDTO updatedPlayerDetails) {
        Long playerId = Long.parseLong(id);
        String responseMessage = playerService.updatePlayer(playerId, updatedPlayerDetails);
        return ResponseEntity.ok(responseMessage);
    }

    /**
     * Retrieves the recent matches of a player.
     * 
     * This endpoint fetches and returns a list of recent matches for the player
     * specified by the "X-User-PlayerId" header.
     * It is intended for displaying the player's recent game history.
     * 
     * @param id The ID of the player, passed as a custom request header
     *           ("X-User-PlayerId").
     * @return ResponseEntity containing a list of MatchResponseDTO objects
     *         representing the player's recent matches.
     * @throws RuntimeException if there is an issue fetching the recent matches for
     *                          the provided player ID.
     */
    @GetMapping("/recentMatches")
    public ResponseEntity<List<MatchResponseDTO>> getRecentMatches(@RequestHeader("X-User-PlayerId") String id) {
        Long playerId = Long.parseLong(id);
        List<MatchResponseDTO> recentMatches = playerService.getRecentMatches(playerId);
        return ResponseEntity.ok(recentMatches);
    }

    /**
     * Endpoint to retrieve a list of player details based on provided player IDs.
     * 
     * This method accepts a list of player IDs in the request body and returns the
     * corresponding player details for each player ID. It calls the service layer
     * to fetch the player details
     * and returns them as a list in the response.
     * 
     * @param playerIds A list of player IDs for which the details are to be
     *                  fetched.
     * @return A ResponseEntity containing a list of PlayerDetailsDTO objects, which
     *         represent the details of the players
     *         whose IDs were provided in the request.
     */
    @PostMapping("/list")
    public ResponseEntity<List<PlayerDetailsDTO>> getListOfPlayerDetails(@RequestBody List<Long> playerIds) {
        List<PlayerDetailsDTO> playerDetails = playerService.getListOfPlayerDetails(playerIds);
        return ResponseEntity.ok(playerDetails);
    }

    /**
     * Endpoint to retrieve the top 100 players based on ranking.
     * 
     * This method calls the service layer to fetch the top 100 players and their
     * ranking details, returning them in a list as a response.
     * 
     * @return A ResponseEntity containing a list of RankingDTO objects,
     *         representing the top 100 players.
     */
    @GetMapping("/top100Players")
    public ResponseEntity<List<RankingDTO>> findTop100Players() {
        return ResponseEntity.ok(playerService.findTop100Players());
    }

    /**
     * Endpoint to retrieve the Elo rating of a specific player.
     * 
     * This method fetches the Elo rating of the player identified by the given ID.
     * 
     * @param id The ID of the player whose Elo rating is to be fetched.
     * @return A ResponseEntity containing the player's Elo rating as an Integer.
     */
    @GetMapping("/elo/{id}")
    public ResponseEntity<Integer> getPlayerElo(@PathVariable("id") long id) {
        Integer elo = playerService.getPlayerElo(id);
        return ResponseEntity.ok(elo);
    }

    /**
     * Endpoint to update the win/loss Elo rating of a specific player.
     * 
     * This method interacts with the Elo service to update the win/loss Elo rating
     * for the player identified by the provided DTO, based on the result of a win
     * or
     * loss.
     * The Elo service handles the logic of updating the Elo rating.
     * 
     * @param dto The DTO containing the player's ID, new Elo rating, and other
     *            necessary
     *            details to perform the update.
     */
    @PutMapping("/winLossElo")
    public void updateWinLossElo(@RequestBody WinLossUpdateDTO dto) {
        playerService.updateWinLossElo(dto);
    }

    /**
     * Endpoint to upload a profile picture for a player.
     * 
     * This method receives the player ID and the file to be uploaded as part of the
     * request.
     * It then calls the service to handle the actual file upload. If the upload is
     * successful, a success message is returned with a 200 OK status. If there is
     * any issue with the upload,an error message is returned with a 500 Internal
     * Server Error status.
     *
     * @param id   The player ID, passed as a custom header ("X-User-PlayerId").
     * @param file The profile picture file, passed as a multipart file.
     * @return A response containing a message indicating whether the upload was
     *         successful or failed.
     */
    @PostMapping("/uploadProfile")
    public ResponseEntity<String> uploadProfilePicture(@RequestHeader("X-User-PlayerId") String id,
            @RequestParam("file") MultipartFile file) {
        Long playerId = Long.parseLong(id);
        String responseMessage = "";
        try {
            responseMessage = playerService.uploadProfilePicture(playerId, file);
            return ResponseEntity.ok("Profile uploaded successfully");
        } catch (IOException e) {
            responseMessage = "File upload failed";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    /**
     * Endpoint to upload a profile picture for a specific player.
     * 
     * This method receives the player ID as part of the URL path and the file to be
     * uploaded as
     * a multipart form data. It then calls the service to handle the actual file
     * upload. If the
     * upload is successful, a success message is returned with a 200 OK status. If
     * there is an
     * issue with the upload (e.g., file-related errors), an error message is
     * returned with a
     * 500 Internal Server Error status.
     *
     * @param id   The player ID, passed as a path variable in the URL.
     * @param file The profile picture file, passed as a multipart file.
     * @return A response containing a message indicating whether the upload was
     *         successful or failed.
     */
    @PostMapping("/uploadProfile/{id}")
    public ResponseEntity<String> uploadProfilePicture(@PathVariable("id") long id,
            @RequestParam("file") MultipartFile file) {
        String responseMessage = "";
        try {
            responseMessage = playerService.uploadProfilePicture(id, file);
            return ResponseEntity.ok(responseMessage);
        } catch (IOException e) {
            responseMessage = "File upload failed";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMessage);
        }
    }

    /**
     * Endpoint to retrieve a player's profile picture based on player ID.
     * 
     * This method retrieves the profile picture for the player identified by the
     * given player ID, and returns the photo in the response body with the correct
     * media type.
     * 
     * @param id The player ID passed as a path variable.
     * @return The player's profile picture as an InputStreamResource, or a 404 if
     *         not found.
     */
    @GetMapping("/photo/{id}")
    public ResponseEntity<InputStreamResource> getPhoto(@PathVariable("id") long id) {
        try {
            String filename = "player_" + id;
            byte[] photoData = playerService.getProfilePicture(filename);

            if (photoData == null || photoData.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return createPhotoResponse(photoData, filename);

        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 for missing picture
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Return 500 for internal issues
        }
    }

    /**
     * Endpoint to retrieve a player's profile picture using the player ID in the
     * request header.
     * 
     * This method retrieves the profile picture for the player identified by the
     * player ID passed in the request header.
     * 
     * @param id The player ID passed as a request header ("X-User-PlayerId").
     * @return The player's profile picture as an InputStreamResource, or a 404 if
     *         not found.
     */
    @GetMapping("/photo")
    public ResponseEntity<InputStreamResource> getPhoto(@RequestHeader("X-User-PlayerId") String id) {
        try {
            String filename = "player_" + id;
            byte[] photoData = playerService.getProfilePicture(filename);

            if (photoData == null || photoData.length == 0) {
                System.out.println("Photo data is null or empty for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return createPhotoResponse(photoData, filename);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Helper method to create the response with the correct media type.
     * 
     * @param photoData The photo data to be returned.
     * @param filename  The filename used to determine the media type.
     * @return The response containing the photo as InputStreamResource.
     */
    private ResponseEntity<InputStreamResource> createPhotoResponse(byte[] photoData, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(determineMediaType(filename));
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(photoData)));
    }

    /**
     * Method to determine media type based on filename extension.
     * 
     * @param filename The filename used to determine the media type.
     * @return The appropriate media type (JPEG, PNG, etc.).
     */
    private MediaType determineMediaType(String filename) {
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Endpoint to retrieve the ranking of a specific player.
     * 
     * This method retrieves the current ranking of the player identified by the
     * player ID passed in the request header. The ranking is determined based on
     * the player's performance and is returned as an integer.
     * 
     * @param id The player ID, passed as a custom header ("X-User-PlayerId").
     * @return The player's ranking as an integer in the response body.
     */
    @GetMapping("/ranking")
    public ResponseEntity<Integer> getRankingForCurrentPlayer(@RequestHeader("X-User-PlayerId") String id) {
        return ResponseEntity.ok(playerService.getRankingForCurrentPlayer(Long.parseLong(id)));
    }

}