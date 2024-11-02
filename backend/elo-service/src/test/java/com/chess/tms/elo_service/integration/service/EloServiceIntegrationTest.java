package com.chess.tms.elo_service.integration.service;


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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

import java.time.LocalDate;
import java.time.LocalDateTime;

@TestPropertySource(properties = {
    "leaderboard.service.url=http://localhost:8087/api/leaderboard",
    "players.service.url=http://localhost:8083/api/players"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EloServiceIntegrationTest {

    @LocalServerPort
    private int port;

    private final String baseUrl = "http://localhost:";

    private final String eloUrl = "/api/elo";

    @Value("${leaderboard.service.url}")
    private String leaderboardServiceUrl;

    @Value("${players.service.url}")
    private String playersServiceUrl;


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
        EloHistory history1 = new EloHistory(1, 12345, 1315, 1315, Reason.WIN, LocalDateTime.now());
        EloHistory history2 = new EloHistory(2, 13333, 1330, 1315, Reason.LOSS, LocalDateTime.now());
        eloRepository.save(history1);
        eloRepository.save(history2);
    }

    @Test
    public void updateMatchPlayersElo_Valid_Success(){
        MatchEloRequestDTO reqDto = new MatchEloRequestDTO(12345, 13333);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<MatchEloRequestDTO> request = new HttpEntity<>(reqDto, headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl + port + eloUrl + "/match",
            HttpMethod.PUT,
            request,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Players' elo updated successfully", response);
        assertEquals(4, eloRepository.findAll().size());
    }
}