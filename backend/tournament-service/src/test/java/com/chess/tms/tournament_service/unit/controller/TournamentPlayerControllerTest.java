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

import static org.mockito.ArgumentMatchers.anyLong;
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
    void testGetTournamentPlayersByTournamentId() throws Exception {
        List<PlayerDetailsDTO> playerDetails = Arrays.asList(
                new PlayerDetailsDTO(1L, 100L, 1500, "John", "Doe", "https://example.com/image1.jpg", 10, 2, 12, 1600, 1400, "USA"),
                new PlayerDetailsDTO(2L, 101L, 1400, "Jane", "Doe", "https://example.com/image2.jpg", 8, 3, 11, 1450, 1350, "Canada")
        );
        
        when(tournamentService.getPlayersByTournament(1L)).thenReturn(playerDetails);

        mockMvc.perform(get("/api/tournament-players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(100L))
                .andExpect(jsonPath("$[1].userId").value(101L));

        verify(tournamentService, times(1)).getPlayersByTournament(1L);
    }

    @Test
    void testDeletePlayerFromTournament() throws Exception {
        doNothing().when(tournamentService).deletePlayerFromTournament(100L, 1L);

        mockMvc.perform(delete("/api/tournament-players/100/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(tournamentService, times(1)).deletePlayerFromTournament(100L, 1L);
    }

    @Test
    void testRegisterPlayer() throws Exception {
        doNothing().when(tournamentService).registerPlayer(100L, 1L);

        mockMvc.perform(post("/api/tournament-players/register/100/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player registered successfully"));

        verify(tournamentService, times(1)).registerPlayer(100L, 1L);
    }

    @Test
    void testDeleteCurrentPlayerFromTournament() throws Exception {
        doNothing().when(tournamentService).deletePlayerFromTournament(100L, 1L);

        mockMvc.perform(delete("/api/tournament-players/current/1")
                .header("X-User-PlayerId", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(tournamentService, times(1)).deletePlayerFromTournament(100L, 1L);
    }

    @Test
    void testRegisterCurrentPlayer() throws Exception {
        doNothing().when(tournamentService).registerPlayer(100L, 1L);

        mockMvc.perform(post("/api/tournament-players/register/current/1")
                .header("X-User-PlayerId", "100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player registered successfully"));

        verify(tournamentService, times(1)).registerPlayer(100L, 1L);
    }

    @Test
    void testGetTournamentPlayersByNonExistentTournament() throws Exception {
        when(tournamentService.getPlayersByTournament(anyLong()))
                .thenThrow(new TournamentDoesNotExistException("Tournament does not exist."));

        mockMvc.perform(get("/api/tournament-players/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tournament does not exist."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testRegisterPlayerWhenPlayerAlreadyRegistered() throws Exception {
        doThrow(new PlayerAlreadyRegisteredException("Player is already registered."))
                .when(tournamentService).registerPlayer(100L, 1L);

        mockMvc.perform(post("/api/tournament-players/register/100/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Player is already registered."))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testDeletePlayerFromNonExistentTournament() throws Exception {
        doThrow(new TournamentDoesNotExistException("Tournament does not exist."))
                .when(tournamentService).deletePlayerFromTournament(100L, 99L);
    
        mockMvc.perform(delete("/api/tournament-players/100/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tournament does not exist."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testRegisterPlayerSuccess() throws Exception {
        mockMvc.perform(post("/api/tournament-players/register/100/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player registered successfully"));

        verify(tournamentService, times(1)).registerPlayer(100L, 1L);
    }

    @Test
    void testDeletePlayerSuccess() throws Exception {
        mockMvc.perform(delete("/api/tournament-players/100/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player deleted successfully"));

        verify(tournamentService, times(1)).deletePlayerFromTournament(100L, 1L);
    }
}