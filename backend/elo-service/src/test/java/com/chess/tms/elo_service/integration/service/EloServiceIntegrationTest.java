package com.chess.tms.elo_service.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;


import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

// @TestPropertySource(properties = {
//     "players.service.url=http://localhost:8083"
// })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EloServiceIntegrationTest {

    // @LocalServerPort
    // private int port;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EloRepository eloRepository;

    @Autowired
    private RestTemplate externalRestTemplate;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.createServer(externalRestTemplate);
        eloRepository.deleteAll();
        
        saveNewEloHistory(1, 1315, 1315, Reason.WIN, LocalDateTime.now());
        saveNewEloHistory(2, 1330, 1315, Reason.LOSS, LocalDateTime.now());

    }

    public void saveNewEloHistory(int playerId, int oldElo, int newElo, Reason reason, LocalDateTime dateTime){ 
        EloHistory eloHistory = new EloHistory();
        eloHistory.setPlayerId(playerId);
        eloHistory.setOldElo(oldElo);
        eloHistory.setNewElo(newElo);
        eloHistory.setChangeReason(reason);
        eloHistory.setCreatedAt(dateTime);

        eloRepository.save(eloHistory);
    }

    @Test
    public void updateMatchPlayersElo_Valid_Success(){
        int winner = 1;
        int loser = 2;
        MatchEloRequestDTO reqDto = new MatchEloRequestDTO(winner, loser);

        System.out.println(String.format("Running updateMatchPlayersElo_Valid_Success with winner %s, loser %s", winner, loser));

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/elo/" + winner)) // mock response from player service
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("1315"));

        System.out.println("Running mocking response from player service for loser");

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/elo/" + loser))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("1315"));
        
        System.out.println("Running mocking response from player service for updateWinLossElo");

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/updateWinLossElo"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/updateWinLossElo"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));
        
        System.out.println("Running mocking response from player service for updateWinLossElo");

            
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        System.out.println("Running Headers:" + headers);
        System.out.println("Running Request Body:" + reqDto);

        HttpEntity<MatchEloRequestDTO> request = new HttpEntity<>(reqDto, headers);
        System.out.println(reqDto.toString());

        System.out.println("Request URL: /api/elo/match");
        System.out.println("Running Request Headers: " + request.getHeaders());
        System.out.println("Running Request Body: " + request.getBody());
        

        ResponseEntity<String> response = restTemplate.exchange(
            "/api/elo/match",
            HttpMethod.PUT,
            request,
            String.class
        );

        System.out.println("Running Response Status: " + response.getStatusCode());
        System.out.println("Running Response Body: " + response.getBody());

        

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Players' elo updated successfully"));
        assertEquals(4, eloRepository.findAll().size());
    }
}