package com.chess.tms.player_service.integration;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.repository.PlayerDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PlayerDetailsServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    @Transactional
    @BeforeEach
    public void setup() {
        playerDetailsRepository.deleteAll(); // Clean up database before each test
    }

    // Test for getting player details by player ID
    @Test
    public void getCurrentPlayerDetails_Success() {
        // Directly create a player in the repository
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setId(1L);
        playerDetails.setUserId(1L); // Assuming userId is the same as id for simplicity
        playerDetails.setEloRating(2000);
        playerDetails.setFirstName("Test");
        playerDetails.setLastName("Player");
        playerDetails.setProfilePicture("test_profile_picture_url");
        playerDetails.setTotalWins(10);
        playerDetails.setTotalLosses(5);
        playerDetails.setTotalMatches(15);
        playerDetails.setHighestElo(2100);
        playerDetails.setCountry("USA");
        playerDetailsRepository.save(playerDetails); // Save directly to the repository

        // Perform GET request to the endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "1");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<PlayerDetailsDTO> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.GET, entity, PlayerDetailsDTO.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Validate the response body
        PlayerDetailsDTO responseDTO = response.getBody();
        assertEquals(1L, responseDTO.getId());
        assertEquals(1L, responseDTO.getUserId());
        assertEquals(2000, responseDTO.getEloRating());
        assertEquals("Test", responseDTO.getFirstName());
        assertEquals("Player", responseDTO.getLastName());
        assertEquals("test_profile_picture_url", responseDTO.getProfilePicture());
        assertEquals(10, responseDTO.getTotalWins());
        assertEquals(5, responseDTO.getTotalLosses());
        assertEquals(15, responseDTO.getTotalMatches());
        assertEquals(2100, responseDTO.getHighestElo());
        assertEquals("USA", responseDTO.getCountry());
    }

    // Test for handling invalid player ID format
    @Test
    public void getCurrentPlayerDetails_InvalidPlayerIdFormat() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "invalid_id");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.GET, entity, String.class);

        // Expect 400 Bad Request due to invalid ID format
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid format for numeric value"));
    }

    // Test for non-existent player ID (not found)
    @Test
    public void getCurrentPlayerDetails_PlayerNotFound() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "9999"); // Assuming player ID 9999 doesn't exist
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.GET, entity, String.class);

        // Expect 404 Not Found if player does not exist
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Player with id 9999 not found"));
    }

    @Test
    public void updatePlayer_Success() {
        // Directly create a player in the repository
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setId(2L);
        playerDetails.setUserId(2L); // Assuming userId is the same as id for simplicity
        playerDetails.setEloRating(2000);
        playerDetails.setFirstName("Test");
        playerDetails.setLastName("Player");
        playerDetails.setProfilePicture("test_profile_picture_url");
        playerDetails.setTotalWins(10);
        playerDetails.setTotalLosses(5);
        playerDetails.setTotalMatches(15);
        playerDetails.setHighestElo(2100);
        playerDetails.setCountry("USA");
        playerDetailsRepository.save(playerDetails); // Save directly to the repository

        // Create updated player details
        UpdatePlayerDetailsDTO updatedPlayerDetails = new UpdatePlayerDetailsDTO();
        updatedPlayerDetails.setFirstName("UpdatedFirstName");
        updatedPlayerDetails.setLastName("UpdatedLastName");
        updatedPlayerDetails.setCountry("Canada");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "2");
        HttpEntity<UpdatePlayerDetailsDTO> entity = new HttpEntity<>(updatedPlayerDetails, headers);

        // Perform PUT request to the endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.PUT, entity, String.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated player with ID 2", response.getBody());

        // Verify the updated details
        PlayerDetails updatedPlayer = playerDetailsRepository.findById(2L).orElseThrow();
        assertEquals("UpdatedFirstName", updatedPlayer.getFirstName());
        assertEquals("UpdatedLastName", updatedPlayer.getLastName());
        assertEquals("Canada", updatedPlayer.getCountry());
    }

    @Test
    public void updatePlayer_PlayerNotFound() {
        // Create updated player details
        UpdatePlayerDetailsDTO updatedPlayerDetails = new UpdatePlayerDetailsDTO();
        updatedPlayerDetails.setFirstName("UpdatedFirstName");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "9999"); // Assume player 9999 doesn't exist
        HttpEntity<UpdatePlayerDetailsDTO> entity = new HttpEntity<>(updatedPlayerDetails, headers);

        // Perform PUT request to the endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.PUT, entity, String.class);

        // Assertions
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("Player with id 9999 not found"));
    }

    @Test
    public void updatePlayer_InvalidPlayerIdFormat() {
        // Create updated player details
        UpdatePlayerDetailsDTO updatedPlayerDetails = new UpdatePlayerDetailsDTO();
        updatedPlayerDetails.setFirstName("UpdatedFirstName");

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "invalid_id"); // Invalid ID format
        HttpEntity<UpdatePlayerDetailsDTO> entity = new HttpEntity<>(updatedPlayerDetails, headers);

        // Perform PUT request to the endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.PUT, entity, String.class);

        // Assertions
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Invalid format for numeric value"));
    }

    @Test
    public void updatePlayer_PartialUpdate() {
        // Directly create a player in the repository
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setId(3L);
        playerDetails.setUserId(3L); 
        playerDetails.setEloRating(2000);
        playerDetails.setFirstName("Test");
        playerDetails.setLastName("Player");
        playerDetails.setProfilePicture("test_profile_picture_url");
        playerDetails.setTotalWins(10);
        playerDetails.setTotalLosses(5);
        playerDetails.setTotalMatches(15);
        playerDetails.setHighestElo(2100);
        playerDetails.setCountry("USA");
        playerDetailsRepository.save(playerDetails); // Save directly to the repository

        // Create updated player details with only some fields
        UpdatePlayerDetailsDTO updatedPlayerDetails = new UpdatePlayerDetailsDTO();
        updatedPlayerDetails.setFirstName("UpdatedFirstName"); // Only update first name

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "3");
        HttpEntity<UpdatePlayerDetailsDTO> entity = new HttpEntity<>(updatedPlayerDetails, headers);

        // Perform PUT request to the endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/player/currentPlayerById", HttpMethod.PUT, entity, String.class);

        // Assertions
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated player with ID 3", response.getBody());

        // Verify that only the updated fields were changed
        PlayerDetails updatedPlayer = playerDetailsRepository.findById(3L).orElseThrow();
        assertEquals("UpdatedFirstName", updatedPlayer.getFirstName());
        assertEquals("Player", updatedPlayer.getLastName()); // Last name should remain unchanged
        assertEquals("USA", updatedPlayer.getCountry()); // Country should remain unchanged
    }
}
