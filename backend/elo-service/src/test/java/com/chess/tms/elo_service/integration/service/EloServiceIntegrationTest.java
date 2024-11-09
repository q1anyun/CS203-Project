package com.chess.tms.elo_service.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;
import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// @TestPropertySource(properties = {
//     "players.service.url=http://localhost:8083"
// })
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class EloServiceIntegrationTest {

    private final int PLAYER_1_ID = 1;

    private final int PLAYER_2_ID = 2;

    private int elo = 1315; // used to help in assertions and pre-setting up of H2 database

    @LocalServerPort
    private int port;

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
        // LocalDateTime[] times = new LocalDateTime[5]; 
        // for(int i = 0; i < 5; i++) {
        //     times[i] = LocalDateTime.now();
        // }
        int elo = 1315; // ued to set old and current elo for pre-inserted EloHistories

        mockServer = MockRestServiceServer.createServer(externalRestTemplate);
        eloRepository.deleteAll();

        for(int i = 0; i < 5; i++) {
            saveNewEloHistory(PLAYER_1_ID, elo, elo + 10, Reason.WIN, LocalDateTime.now());
            elo += 10;
        }
    
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

    public void assertResponseListEquals(List<EloResponseDTO> list, int elo) {
        for(int i = 0; i < 5; i++) {
            assertEquals(list.get(i).getPlayerId(), 1);
            assertEquals(list.get(i).getOldElo(), elo);
            assertEquals(list.get(i).getNewElo(), elo + 10);
            assertEquals(list.get(i).getChangeReason(), Reason.WIN);
            elo += 10;
        }
    }

    public void assertResponseListEqualsReverse(List<EloResponseDTO> list, int elo) {
        for(int i = 4; i >= 0;  i--) {
            assertEquals(list.get(i).getPlayerId(), 1);
            assertEquals(list.get(i).getOldElo(), elo);
            assertEquals(list.get(i).getNewElo(), elo + 10);
            assertEquals(list.get(i).getChangeReason(), Reason.WIN);
            elo += 10;
        }
    }

    @Test
    public void updateMatchPlayersElo_Valid_Success(){
        MatchEloRequestDTO reqDto = new MatchEloRequestDTO(PLAYER_1_ID, PLAYER_2_ID);

        System.out.println(String.format("Running updateMatchPlayersElo_Valid_Success with winner %s, loser %s", PLAYER_1_ID, PLAYER_2_ID));

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/elo/" + PLAYER_1_ID)) // mock response from player service
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("1315"));

        System.out.println("Running mocking response from player service for loser");

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/elo/" + PLAYER_2_ID))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("1315"));
        
        System.out.println("Running mocking response from player service for updateWinLossElo");

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/winLossElo"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));

        mockServer.expect(requestTo(playerServiceUrl + "/api/player/winLossElo"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withStatus(HttpStatus.OK));
        
        System.out.println("Running mocking response from player service for winLossElo");

            
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);
    
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
        assertEquals(8, eloRepository.findAll().size());
    }

    @Test
    public void deleteMatchPlayerElo_Valid_Success(){
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        HttpEntity<Integer> request = new HttpEntity<>(1, headers);

        ResponseEntity<List<EloResponseDTO>> response = restTemplate.exchange(
            "/api/elo/deletion/1",
            HttpMethod.DELETE,
            request,
            new ParameterizedTypeReference<List<EloResponseDTO>>(){}
        );

        List<EloResponseDTO> ans = response.getBody();
        int elo = 1315;
        assertEquals(HttpStatus.OK, response.getStatusCode());
        for(int i = 0; i < ans.size(); i++) {
            assertEquals(ans.get(i).getPlayerId(), 1);
            assertEquals(ans.get(i).getOldElo(), elo);
            assertEquals(ans.get(i).getNewElo(), elo + 10);
            assertEquals(ans.get(i).getChangeReason(), Reason.WIN);
            elo += 10;
        }
        assertEquals(1, eloRepository.findAll().size());
   
    }

    @Test
    public void deleteMatchPlayerElo_InvalidId_Exception(){
        int playerId = 3;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);
        String msg = "";
        String expectedMsg = "Player with player id " + playerId + " has no history";
    
        HttpEntity<Integer> request = new HttpEntity<>(1, headers);

        try {
            ResponseEntity<List<EloResponseDTO>> response = restTemplate.exchange(
                "/api/elo/deletion/" + playerId,
                HttpMethod.DELETE,
                request,
                new ParameterizedTypeReference<List<EloResponseDTO>>(){}
            );
        } catch (PlayerHistoryNotFoundException e) {
            msg = e.getMessage();
        }
        
        assertEquals(6, eloRepository.findAll().size());
        assertEquals(msg, "");
    }

    @Test
    public void findAllEloHistory_Valid_Success() throws Exception{
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Integer> request = new HttpEntity<>(headers);

        ResponseEntity<List<EloResponseDTO>> response =  restTemplate.exchange(
            "/api/elo",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<List<EloResponseDTO>>(){}
        );
        List<EloResponseDTO> ans = response.getBody();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        int elo = 1315;
        for(int i = 0; i < 5; i++) {
            assertEquals(ans.get(i).getPlayerId(), 1);
            assertEquals(ans.get(i).getOldElo(), elo);
            assertEquals(ans.get(i).getNewElo(), elo + 10);
            assertEquals(ans.get(i).getChangeReason(), Reason.WIN);
            elo += 10;
        }
        assertEquals(ans.get(5).getPlayerId(), 2);
        assertEquals(ans.get(5).getOldElo(), 1330); 
        assertEquals(ans.get(5).getNewElo(), 1315);
        assertEquals(ans.get(5).getChangeReason(), Reason.LOSS);
        assertEquals(6, response.getBody().size());
    }

    @Test 
    void findAllByPlayerId_Valid_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Optional<List<EloResponseDTO>>> eloResponseOptional = restTemplate.exchange(
            "/api/elo/1",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Optional<List<EloResponseDTO>>>(){}
        );

        List<EloResponseDTO> eloResponseList = eloResponseOptional.getBody().get();
        assertResponseListEquals(eloResponseList, elo);
        assertEquals(eloResponseList.size(), 5);
        assertEquals(HttpStatus.OK, eloResponseOptional.getStatusCode());
    }

    @Test 
    void findAllByPlayerId_InvalidId_Exception() {
        String exceptionMsg = "";
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Optional<List<EloResponseDTO>>> eloResponseOptional = new ResponseEntity<>(HttpStatus.OK);
        
        try {
            eloResponseOptional = restTemplate.exchange(
            "/api/elo/3",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Optional<List<EloResponseDTO>>>(){}
        );
        } catch (PlayerHistoryNotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals(exceptionMsg,  "");
    }

    @Test 
    void findAllEloHistoriesByPlayerIdAndChangeReason_Id1_Win_Success() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", "1");
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<Optional<List<EloResponseDTO>>> eloResponseOptional = restTemplate.exchange(
            "/api/elo/1/win",
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Optional<List<EloResponseDTO>>>(){}
        );

        List<EloResponseDTO> eloResponseList = eloResponseOptional.getBody().get();
        assertResponseListEqualsReverse(eloResponseList, 1315);
        assertEquals(eloResponseList.size(), 5);
        assertEquals(HttpStatus.OK, eloResponseOptional.getStatusCode());
    }






//    @Test
//    public void findCurrentPlayerEloHistoryForChart_Valid_LessThan5Success() throws Exception{
//     HttpHeaders headers = new HttpHeaders();
//     headers.set("X-User-PlayerId", "2");
//     headers.setContentType(MediaType.APPLICATION_JSON);

//     HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

//         ResponseEntity<EloHistoryChartDTO[]> response = restTemplate.exchange(
//             "/api/elo/chart/current",
//             HttpMethod.GET,
//             httpRequest, 
//             EloHistoryChartDTO[].class);

//         List<EloHistoryChartDTO> expected = new ArrayList<>();
//         expected.add(new EloHistoryChartDTO(1315, LocalDate.now()));

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertArrayEquals(expected.toArray(new EloHistoryChartDTO[1]), response.getBody());
//         assertEquals(1, response.getBody().length);
//     }

//     @Test
//    public void findCurrentPlayerEloHistoryForChart_Valid_MoreThan5Success() throws Exception{
//     HttpHeaders headers = new HttpHeaders();
//     headers.set("X-User-PlayerId", "1");
//     headers.setContentType(MediaType.APPLICATION_JSON);

//     HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

//         ResponseEntity<EloHistoryChartDTO[]> response = restTemplate.exchange(
//             "/api/elo/chart/current",
//             HttpMethod.GET,
//             httpRequest, 
//             EloHistoryChartDTO[].class);

//         List<EloHistoryChartDTO> expected = new ArrayList<>();
//         expected.add(new EloHistoryChartDTO(1365, LocalDate.now()));
//         expected.add(new EloHistoryChartDTO(1330, LocalDate.now()));
//         expected.add(new EloHistoryChartDTO(1340, LocalDate.now()));
//         expected.add(new EloHistoryChartDTO(1350, LocalDate.now()));        
//         expected.add(new EloHistoryChartDTO(1330, LocalDate.now()));

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertArrayEquals(expected.toArray(new EloHistoryChartDTO[5]), response.getBody());
//         assertEquals(5, response.getBody().length);
//     }

//     @Test 
//     public void findPlayerEloHistoryForChart_Valid_MoreThan5_Success() throws Exception{
//         // String exceptionMsg = "";
//         // String expectedExceptionMsg = "Player with player id 3 has no history";
//         long playerId = 1;
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("X-User-PlayerId", "1");
//         headers.setContentType(MediaType.APPLICATION_JSON);

//         HttpEntity<Void> httpRequest = new HttpEntity<>(headers);

//         ResponseEntity<EloHistoryChartDTO[]> response = new ResponseEntity<>(HttpStatus.OK);

//             response = restTemplate.exchange(
//                 "/api/elo/chart/" + playerId,
//                 HttpMethod.GET,
//                 httpRequest, 
//                 EloHistoryChartDTO[].class);

//                 List<EloHistoryChartDTO> expected = new ArrayList<>();
//                 expected.add(new EloHistoryChartDTO(1365, LocalDate.now()));
//                 expected.add(new EloHistoryChartDTO(1330, LocalDate.now()));
//                 expected.add(new EloHistoryChartDTO(1340, LocalDate.now()));
//                 expected.add(new EloHistoryChartDTO(1350, LocalDate.now()));        
//                 expected.add(new EloHistoryChartDTO(1330, LocalDate.now()));

//         assertEquals(HttpStatus.OK, response.getStatusCode());
//         assertArrayEquals(expected.toArray(new EloHistoryChartDTO[5]), response.getBody());
//         assertEquals(5, response.getBody().length);
//     }

//     @Test 
//     public void findPlayerEloHistoryForChart_Valid_LessThan5_Success() throws Exception{
//         long playerId = 2;
//         HttpHeaders headers = new HttpHeaders();
//         headers.set("X-User-PlayerId", "2");
//         headers.setContentType(MediaType.APPLICATION_JSON);
    
//         HttpEntity<Void> httpRequest = new HttpEntity<>(headers);
    
//             ResponseEntity<EloHistoryChartDTO[]> response = restTemplate.exchange(
//                 "/api/elo/chart/" + playerId,
//                 HttpMethod.GET,
//                 httpRequest, 
//                 EloHistoryChartDTO[].class);
    
//             List<EloHistoryChartDTO> expected = new ArrayList<>();
//             expected.add(new EloHistoryChartDTO(1315, LocalDate.now()));
    
//             assertEquals(HttpStatus.OK, response.getStatusCode());
//             assertArrayEquals(expected.toArray(new EloHistoryChartDTO[1]), response.getBody());
//             assertEquals(1, response.getBody().length);
//     }

    
}