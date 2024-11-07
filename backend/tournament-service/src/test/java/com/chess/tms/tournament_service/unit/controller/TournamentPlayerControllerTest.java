package com.chess.tms.tournament_service.unit.controller;

import com.chess.tms.tournament_service.controller.TournamentPlayerController;
import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.exception.GlobalExceptionHandler;
import com.chess.tms.tournament_service.exception.PlayerAlreadyRegisteredException;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TournamentPlayerController.class)
@ExtendWith(MockitoExtension.class)
class TournamentPlayerControllerTest {

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

    @Test
    void getTournamentPlayersByTournamentId_ValidTournamentId_ReturnPlayerDetails() throws Exception {
        List<PlayerDetailsDTO> playerDetails = List.of(
                new PlayerDetailsDTO(1L, 100L, 1500, "John", "Doe", "https://example.com/image1.jpg", 10, 2, 12, 1600, "USA"),
                new PlayerDetailsDTO(2L, 101L, 1400, "Jane", "Doe", "https://example.com/image2.jpg", 8, 3, 11, 1450, "Canada")
        );

        when(tournamentService.getPlayersByTournament(1L)).thenReturn(playerDetails);

        mockMvc.perform(get("/api/tournament-players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100L))
                .andExpect(jsonPath("$[1].userId").value(101L));

        verify(tournamentService).getPlayersByTournament(1L);
    }

    @Test
    void deletePlayerFromTournament_ValidTournamentId_Success() throws Exception {
        testDeletePlayerFromTournament(100L, 1L);
    }

    @Test
    void registerPlayer_ValidIds_Success() throws Exception {
        testRegisterPlayer(100L, 1L, "Player registered successfully");
    }

    @Test
    void deleteCurrentPlayerFromTournament_ValidTournamentId_Success() throws Exception {
        testDeletePlayerFromTournament(100L, 1L, "X-User-PlayerId", 100L);
    }

    @Test
    void registerCurrentPlayer_ValidTournamentId_Success() throws Exception {
        testRegisterPlayer(100L, 1L, "Player registered successfully", "X-User-PlayerId", 100L);
    }

    @Test
    void getTournamentPlayersByTournament_InvalidTournamentId_ThrowsException() throws Exception {
        testExceptionScenario(get("/api/tournament-players/99"),
                TournamentDoesNotExistException.class,
                "Tournament does not exist.", 404);
    }

    @Test
    void registerPlayer_InvalidTournamentPlayerId_ThrowsException() throws Exception {
        testExceptionScenario(post("/api/tournament-players/register/100/1"),
                PlayerAlreadyRegisteredException.class,
                "Player is already registered.", 409);
    }

    @Test
    void deletePlayerFromTournament_InvalidTournamentId_ThrowsException() throws Exception {
        testExceptionScenario(delete("/api/tournament-players/100/99"),
                TournamentDoesNotExistException.class,
                "Tournament does not exist.", 404);
    }

    private void testDeletePlayerFromTournament(long playerId, long tournamentId) throws Exception {
        testDeletePlayerFromTournament(playerId, tournamentId, null, null);
    }

    private void testDeletePlayerFromTournament(long playerId, long tournamentId, String header, Long headerValue) throws Exception {
        doNothing().when(tournamentService).deletePlayerFromTournament(playerId, tournamentId);

        var requestBuilder = delete("/api/tournament-players/" + playerId + "/" + tournamentId);
        if (header != null && headerValue != null) {
            requestBuilder = requestBuilder.header(header, headerValue.toString());
        }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(tournamentService).deletePlayerFromTournament(playerId, tournamentId);
    }

    private void testRegisterPlayer(long playerId, long tournamentId, String expectedMessage) throws Exception {
        testRegisterPlayer(playerId, tournamentId, expectedMessage, null, null);
    }

    private void testRegisterPlayer(long playerId, long tournamentId, String expectedMessage, String header, Long headerValue) throws Exception {
        doNothing().when(tournamentService).registerPlayer(playerId, tournamentId);

        var requestBuilder = post("/api/tournament-players/register/" + playerId + "/" + tournamentId);
        if (header != null && headerValue != null) {
            requestBuilder = requestBuilder.header(header, headerValue.toString());
        }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        verify(tournamentService).registerPlayer(playerId, tournamentId);
    }

    private void testExceptionScenario(MockHttpServletRequestBuilder requestBuilder, Class<? extends Exception> exceptionClass,
                                       String expectedMessage, int expectedStatus) throws Exception {
        doThrow(exceptionClass.getConstructor(String.class).newInstance(expectedMessage)).when(tournamentService)
                .deletePlayerFromTournament(anyLong(), anyLong());

        mockMvc.perform(requestBuilder)
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
