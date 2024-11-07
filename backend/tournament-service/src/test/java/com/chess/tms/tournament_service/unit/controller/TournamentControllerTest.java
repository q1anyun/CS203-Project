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
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String TOURNAMENT_JSON = """
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

    private static final String MISSING_NAME_JSON = """
            {
                "startDate": "2024-01-01T10:00:00",
                "endDate": "2024-01-02T10:00:00",
                "minElo": 1000,
                "maxElo": 2000,
                "maxPlayers": 16,
                "timeControl": 5
            }
            """;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createTournament_ValidInput_ReturnSuccess() throws Exception {
        when(tournamentService.createTournament(any(TournamentRegistrationDTO.class), eq(1L)))
                .thenReturn("Tournament created successfully");

        mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TOURNAMENT_JSON)
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament created successfully"));

        verify(tournamentService, times(1)).createTournament(any(TournamentRegistrationDTO.class), eq(1L));
    }

    @Test
    void createTournament_MissingFields_ReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(MISSING_NAME_JSON)
                .header("X-User-Id", "1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(tournamentService);
    }

    @Test
    void startTournament_ValidTournamentId_ReturnSuccess() throws Exception {
        when(tournamentService.startTournament(1L)).thenReturn("Tournament started successfully");

        mockMvc.perform(post("/api/tournaments/start/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament started successfully"));

        verify(tournamentService, times(1)).startTournament(1L);
    }

    @Test
    void getTournament_ValidTournamentId_ReturnsTournamentDetails() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO(1L, "Test Tournament");

        when(tournamentService.getTournamentDetailsById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Tournament"));

        verify(tournamentService, times(1)).getTournamentDetailsById(1L);
    }

    @Test
    void getAllTournaments_ReturnsTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO(1L, "Test Tournament");

        when(tournamentService.getAllTournaments()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getAllTournaments();
    }

    @Test
    void updateTournament_ValidInputs_ReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/tournaments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TOURNAMENT_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament updated successfully"));

        verify(tournamentService, times(1)).updateTournament(eq(1L), any(TournamentUpdateRequestDTO.class));
    }

    @Test
    void deleteTournament_ValidTournamentId_ReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deleted tournament"));

        verify(tournamentService, times(1)).deleteTournament(1L);
    }

    @Test
    void updateCurrentRoundForTournament_ValidRoundId_ReturnSuccess() throws Exception {
        mockMvc.perform(put("/api/tournaments/1/round/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current round updated to 2"));

        verify(tournamentService, times(1)).updateCurrentRoundForTournament(1L, 2L);
    }

    @Test
    void completeTournament_ValidIds_ReturnSuccess() throws Exception {
        when(tournamentService.completeTournament(1L, 2L)).thenReturn("Tournament has been completed");

        mockMvc.perform(put("/api/tournaments/1/winner/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament has been completed"));

        verify(tournamentService, times(1)).completeTournament(1L, 2L);
    }

    @Test
    void getRegisteredTournaments_ValidPlayerId_ReturnsTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO(1L, "Test Tournament");

        when(tournamentService.getRegisteredTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/registered/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getRegisteredTournaments(1L);
    }

    @Test
    void getRegisteredTournamentsCurrentPlayer_ValidPlayerId_ReturnsTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO(1L, "Test Tournament");

        when(tournamentService.getRegisteredTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/registered/current")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Tournament"));

        verify(tournamentService, times(1)).getRegisteredTournaments(1L);
    }

    @Test
    void getLiveTournamentsCurrent_ValidPlayerId_ReturnsLiveTournaments() throws Exception {
        TournamentDetailsDTO dto = new TournamentDetailsDTO(1L, "Live Tournament");

        when(tournamentService.getLiveTournaments(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/tournaments/live/current")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Live Tournament"));

        verify(tournamentService, times(1)).getLiveTournaments(1L);
    }
}
