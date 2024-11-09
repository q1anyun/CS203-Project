package com.chess.tms.tournament_service.unit.controller;

import com.chess.tms.tournament_service.controller.TournamentPlayerController;
import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.exception.GlobalExceptionHandler;
import com.chess.tms.tournament_service.exception.PlayerAlreadyRegisteredException;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.service.TournamentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TournamentPlayerController.class)
@ExtendWith(MockitoExtension.class)
class TournamentPlayerControllerTest {

    // Constants
    private static final Long VALID_TOURNAMENT_ID = 1L;
    private static final Long INVALID_TOURNAMENT_ID = 99L;
    private static final Long VALID_PLAYER_ID = 100L;
    private static final String PLAYER_HEADER = "X-User-PlayerId";
    private static final String BASE_URL = "/api/tournament-players";
    
    private static final List<PlayerDetailsDTO> SAMPLE_PLAYERS = Arrays.asList(
            new PlayerDetailsDTO(1L, 100L, 1500, "John", "Doe", "https://example.com/image1.jpg", 10, 2, 12, 1600, "USA"),
            new PlayerDetailsDTO(2L, 101L, 1400, "Jane", "Doe", "https://example.com/image2.jpg", 8, 3, 11, 1450, "Canada")
    );

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentPlayerController tournamentPlayerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentPlayerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static Stream<Arguments> endpointTestCases() {
        return Stream.of(
            Arguments.of(BASE_URL + "/register/current/" + VALID_TOURNAMENT_ID, "POST", "Player registered successfully"),
            Arguments.of(BASE_URL + "/current/" + VALID_TOURNAMENT_ID, "DELETE", "Player deleted successfully")
        );
    }

    private void performRequestAndExpectError(String url, String method, String errorMessage, int statusCode) throws Exception {
        var requestBuilder = switch (method) {
            case "POST" -> post(url);
            case "DELETE" -> delete(url);
            default -> get(url);
        };

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @ParameterizedTest
    @MethodSource("endpointTestCases")
    void currentPlayerEndpoints_ValidInput_Success(String endpoint, String method, String expectedResponse) throws Exception {
        if (method.equals("POST")) {
            doNothing().when(tournamentService).registerPlayer(VALID_PLAYER_ID, VALID_TOURNAMENT_ID);
        } else {
            doNothing().when(tournamentService).deletePlayerFromTournament(VALID_PLAYER_ID, VALID_TOURNAMENT_ID);
        }

        var requestBuilder = method.equals("POST") ? 
            post(endpoint).header(PLAYER_HEADER, VALID_PLAYER_ID.toString()) :
            delete(endpoint).header(PLAYER_HEADER, VALID_PLAYER_ID.toString());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void getPlayers_ValidTournamentId_ReturnsPlayersList() throws Exception {
        when(tournamentService.getPlayersByTournament(VALID_TOURNAMENT_ID)).thenReturn(SAMPLE_PLAYERS);

        mockMvc.perform(get(BASE_URL + "/" + VALID_TOURNAMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(SAMPLE_PLAYERS.get(0).getUserId()))
                .andExpect(jsonPath("$[1].userId").value(SAMPLE_PLAYERS.get(1).getUserId()));
    }

    @Test
    void getTournamentPlayersByTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() throws Exception {
        when(tournamentService.getPlayersByTournament(anyLong()))
                .thenThrow(new TournamentDoesNotExistException("Tournament does not exist."));

        performRequestAndExpectError(BASE_URL + "/" + INVALID_TOURNAMENT_ID, "GET", 
                "Tournament does not exist.", 404);
    }

    @Test
    void registerPlayer_InvalidTournamentPlayerId_ThrowsPlayerAlreadyRegisteredException() throws Exception {
        doThrow(new PlayerAlreadyRegisteredException("Player is already registered."))
                .when(tournamentService).registerPlayer(100L, 1L);

        performRequestAndExpectError(BASE_URL + "/register/100/1", "POST", 
                "Player is already registered.", 409);
    }

    @Test
    void deletePlayerFromTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() throws Exception {
        doThrow(new TournamentDoesNotExistException("Tournament does not exist."))
                .when(tournamentService).deletePlayerFromTournament(100L, 99L);
    
        performRequestAndExpectError(BASE_URL + "/100/99", "DELETE", 
                "Tournament does not exist.", 404);
    }

    @Test
    void deletePlayer_ValidTournamentPlayerId_Success() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/100/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(tournamentService, times(1)).deletePlayerFromTournament(100L, 1L);
    }
}