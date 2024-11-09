package com.chess.tms.tournament_service.integration.service;

import com.chess.tms.tournament_service.dto.*;
import com.chess.tms.tournament_service.enums.Format;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import com.chess.tms.tournament_service.model.TournamentType;
import com.chess.tms.tournament_service.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TournamentServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentPlayerRepository tournamentPlayerRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private TournamentTypeRepository tournamentTypeRepository;

    @Autowired
    private RestTemplate externalRestTemplate;

    private MockRestServiceServer mockServer;

    @Value("${matches.service.url}")
    private String matchServiceUrl;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(externalRestTemplate);
        tournamentRepository.deleteAll();
        tournamentPlayerRepository.deleteAll();

        if (gameTypeRepository.findById(1L).isEmpty()) {
            GameType gameType = new GameType();
            gameType.setId(1L);
            gameType.setName("Blitz");
            gameType.setTimeControlMinutes(5);
            gameTypeRepository.save(gameType);
        }

        if (roundTypeRepository.findById(1L).isEmpty()) {
            RoundType roundType = new RoundType();
            roundType.setId(1L);
            roundType.setRoundName("Top 16");
            roundType.setNumberOfPlayers(16);
            roundTypeRepository.save(roundType);
        }

        if (tournamentTypeRepository.findById(1L).isEmpty()) {
            TournamentType tournamentType = new TournamentType();
            tournamentType.setId(1L);
            tournamentType.setTypeName("Knockout");
            tournamentTypeRepository.save(tournamentType);
        }
    }

    @Test
    public void createTournament_Valid_Success() {
        TournamentRegistrationDTO dto = new TournamentRegistrationDTO(
        "Test Tournament",
        LocalDate.now().plusDays(1),     // Start Date
        LocalDate.now().plusDays(2),     // End Date
        1000,                            // Minimum Elo
        2000,                            // Maximum Elo
        16,                              // Maximum Players
        1,                               // Game Type ID
        1L,                              // Tournament Type ID
        "Test Description",              // Description
        "Test Photo",                    // Photo URL or ID
        "ONLINE",                        // Format (e.g., "ONLINE", "PHYSICAL", "HYBRID")
        "Singapore",                     // Country
        "Test Address",                  // Location Address
        1.0,                               // Latitude
        1.0                              // Longitude
    );
    
        // Set the headers with the X-User-Id value
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<TournamentRegistrationDTO> request = new HttpEntity<>(dto, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/tournaments",
            request,
            String.class
        );
    
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Tournament created successfully"));
        assertEquals(1, tournamentRepository.findAll().size());
    }

    @Test
    public void startTournament_Valid_Success() {
        Tournament tournament = createTournament();
        tournament.setTournamentType(tournamentTypeRepository.findById(1L).orElseThrow());
        registerPlayerForTournament(tournament, 100L);
        registerPlayerForTournament(tournament, 101L);

        // Mock the external match service response for knockout tournament
        mockServer.expect(requestTo(
            matchServiceUrl + "/api/matches/knockout/" + tournament.getTournamentId() + "/1"))
            .andRespond(withStatus(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("1"));

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/tournaments/start/" + tournament.getTournamentId(),
            null,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("current round is"));

        Tournament startedTournament = tournamentRepository.findById(tournament.getTournamentId()).orElseThrow();
        assertEquals(Status.LIVE, startedTournament.getStatus());
    }

    @Test
    public void startTournament_Error_Failure() {
        Tournament tournament = createTournament();
        tournament.setTournamentType(tournamentTypeRepository.findById(1L).orElseThrow());
        registerPlayerForTournament(tournament, 100L);
        registerPlayerForTournament(tournament, 101L);

        // Mock the external match service response for knockout tournament
        mockServer.expect(requestTo(
            matchServiceUrl + "/api/matches/knockout/" + tournament.getTournamentId() + "/1"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"message\": \"Failed to start tournament due to match service error\"}"));

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/api/tournaments/start/" + tournament.getTournamentId(),
            null,
            String.class
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("Failed to start tournament due to match service error"));

        Tournament failedTournament = tournamentRepository.findById(tournament.getTournamentId()).orElseThrow();
        assertEquals(Status.UPCOMING, failedTournament.getStatus());
    }

    @Test
    public void getTournamentById_ValidTournamentId_Success() {
        Tournament tournament = createTournament();

        ResponseEntity<TournamentDetailsDTO> response = restTemplate.getForEntity(
            "/api/tournaments/" + tournament.getTournamentId(),
            TournamentDetailsDTO.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(tournament.getTournamentId(), response.getBody().getId());
    }

    @Test
    public void getAllTournaments_Valid_Success() {
        createTournament();
        createTournament();

        ResponseEntity<TournamentDetailsDTO[]> response = restTemplate.getForEntity(
            "/api/tournaments",
            TournamentDetailsDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    public void updateCurrentRound_ValidIds_Success() {
        Tournament tournament = createTournament();

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/tournaments/" + tournament.getTournamentId() + "/round/1",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Current round updated to 1"));
    }

    @Test
    public void completeTournament_ValidTournamentId_Success() {
        Tournament tournament = createTournament();

        registerPlayerForTournament(tournament, 109L);

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/tournaments/" + tournament.getTournamentId() + "/winner/109",
            HttpMethod.PUT,
            null,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("has been completed"));

        Tournament completedTournament = tournamentRepository.findById(tournament.getTournamentId()).orElseThrow();
        assertEquals(Status.COMPLETED, completedTournament.getStatus());
    }

    @Test
    public void getRegisteredTournaments_ValidPlayerId_Success() {
        Tournament tournament = createTournament();
        registerPlayerForTournament(tournament, 100L);

        ResponseEntity<TournamentDetailsDTO[]> response = restTemplate.getForEntity(
            "/api/tournaments/registered/100",
            TournamentDetailsDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
    }

    @Test
    public void getLiveTournaments_ValidPlayerId_Success() {
        Tournament tournament = createTournament();
        tournament.setStatus(Status.LIVE);
        registerPlayerForTournament(tournament, 100L);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-PlayerId", "100");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<TournamentDetailsDTO[]> response = restTemplate.exchange(
            "/api/tournaments/live/current",
            HttpMethod.GET,
            entity,
            TournamentDetailsDTO[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
    }

private Tournament createTournament() {
    Tournament tournament = new Tournament();
    tournament.setCreatorId(1L);
    tournament.setName("Integration Tournament");
    tournament.setMaxPlayers(32);
    tournament.setCurrentPlayers(0);
    tournament.setTimeControl(gameTypeRepository.findById(1L).orElseThrow());

    tournament.setTournamentType(tournamentTypeRepository.findById(1L).orElseThrow());
    tournament.setTimeControl(gameTypeRepository.findById(1L).orElseThrow());
    tournament.setStatus(Status.UPCOMING);
    tournament.setStartDate(LocalDate.now().plusDays(1));
    tournament.setEndDate(LocalDate.now().plusDays(2));
    tournament.setMinElo(1300); // Example minimum Elo
    tournament.setMaxElo(2200); // Example maximum Elo
    tournament.setDescription("An exciting integration test tournament.");
    tournament.setPhoto("https://example.com/photos/integration-tournament.jpg");
    tournament.setFormat(Format.PHYSICAL);
    tournament.setCountry("USA");
    tournament.setLocationAddress("123 Chess Avenue, Chess City");
    tournament.setLocationLatitude(37.7749); // Example latitude
    tournament.setLocationLongitude(-122.4194); // Example longitude

    return tournamentRepository.save(tournament);
}

    private void registerPlayerForTournament(Tournament tournament, long playerId) {
        TournamentPlayer player = new TournamentPlayer();
        player.setPlayerId(playerId);
        player.setTournament(tournament);
        tournamentPlayerRepository.save(player);

        tournament.setCurrentPlayers(tournament.getCurrentPlayers() + 1);
        tournamentRepository.save(tournament);
    }
}