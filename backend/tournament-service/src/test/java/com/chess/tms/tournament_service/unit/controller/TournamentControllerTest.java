package com.chess.tms.tournament_service.unit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chess.tms.tournament_service.controller.TournamentController;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.exception.GlobalExceptionHandler;
import com.chess.tms.tournament_service.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.Matchers.containsString;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testCreateTournament() throws Exception {
        String jsonPayload = """
                {
                    "name": "Test Tournament",
                    "startDate": "2024-01-01T10:00:00",
                    "endDate": "2024-01-02T10:00:00",
                    "minElo": 1000,
                    "maxElo": 2000,
                    "maxPlayers": 16,
                    "timeControl": 5
                }
                """;

        when(tournamentService.createTournament(any(TournamentRegistrationDTO.class), eq(1L)))
                .thenReturn("Tournament created successfully");

        mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament created successfully"));

        verify(tournamentService, times(1)).createTournament(any(TournamentRegistrationDTO.class), eq(1L));
    }

    @Test
    void testCreateTournamentWithMissingName() throws Exception {
        String jsonPayload = """
                {
                    "startDate": "2024-01-01T10:00:00",
                    "endDate": "2024-01-02T10:00:00",
                    "minElo": 1000,
                    "maxElo": 2000,
                    "maxPlayers": 16,
                    "timeControl": 5
                }
                """;

        mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest());
        verify(tournamentService, times(0)).createTournament(any(TournamentRegistrationDTO.class), eq(1L));
    }

    @Test
    void testStartTournament() throws Exception {
        when(tournamentService.startTournament(1L)).thenReturn("Tournament started successfully");

        mockMvc.perform(post("/api/tournaments/start/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament started successfully"));

        verify(tournamentService, times(1)).startTournament(1L);
    }

    @Test
    void testGetTournament() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(1L);
        dto.setName("Test Tournament");

        when(tournamentService.getTournamentDetailsById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Tournament"));

        verify(tournamentService, times(1)).getTournamentDetailsById(1L);
    }

    @Test
    void testGetAllTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(1L);
        dto.setName("Test Tournament");

        when(tournamentService.getAllTournaments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getAllTournaments();
    }

    @Test
    void testUpdateTournament() throws Exception {
        String jsonPayload = """
                {
                    "name": "Test Tournament",
                    "startDate": "2024-01-01T10:00:00",
                    "endDate": "2024-01-02T10:00:00",
                    "minElo": 1000,
                    "maxElo": 2000,
                    "maxPlayers": 16,
                    "timeControl": 5
                }
                """;

        mockMvc.perform(put("/api/tournaments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament updated successfully"));

        verify(tournamentService, times(1)).updateTournament(eq(1L), any(TournamentUpdateRequestDTO.class));
    }

    @Test
    void testDeleteTournament() throws Exception {
        mockMvc.perform(delete("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sucessfully deleted tournament"));

        verify(tournamentService, times(1)).deleteTournament(1L);
    }

    @Test
    void testUpdateCurrentRoundForTournament() throws Exception {
        mockMvc.perform(put("/api/tournaments/1/round/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current round updated to 2"));

        verify(tournamentService, times(1)).updateCurrentRoundForTournament(1L, 2L);
    }

    @Test
    void testCompleteTournament() throws Exception {
        when(tournamentService.completeTournament(1L, 2L)).thenReturn("Tournament has been completed");

        mockMvc.perform(put("/api/tournaments/1/winner/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament has been completed"));

        verify(tournamentService, times(1)).completeTournament(1L, 2L);
    }

    @Test
    void testGetRegisteredTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(1L);
        dto.setName("Test Tournament");

        when(tournamentService.getRegisteredTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/registered/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getRegisteredTournaments(1L);
    }

    @Test
    void testGetRegisteredTournamentsCurrent() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(1L);
        dto.setName("Test Tournament");

        when(tournamentService.getRegisteredTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/registered/current")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getRegisteredTournaments(1L);
    }

    @Test
    void testGetLiveTournamentsCurrent() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(1L);
        dto.setName("Live Tournament");

        when(tournamentService.getLiveTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/live/current")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Live Tournament"));

        verify(tournamentService, times(1)).getLiveTournaments(1L);
    }
}