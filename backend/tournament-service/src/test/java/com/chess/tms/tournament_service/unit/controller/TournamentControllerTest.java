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

    // Constants
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_TOURNAMENT_ID = 1L;
    private static final Long TEST_PLAYER_ID = 1L;
    
    private static final String BASE_URL = "/api/tournaments";
    
    private static final TournamentDetailsDTO TEST_TOURNAMENT = createTestTournament();
    private static final String VALID_TOURNAMENT_PAYLOAD = """
            {
                "name": "Chess Masters 2024",
                "startDate": "2024-01-01",
                "endDate": "2024-01-02",
                "minElo": 1000,
                "maxElo": 2000,
                "maxPlayers": 16,
                "timeControl": 5,
                "tournamentType": 1,
                "description": "Annual Chess Masters Tournament",
                "photo": "tournament-photo-url",
                "format": "Swiss",
                "country": "Singapore",
                "locationAddress": "123 Singapore Street",
                "locationLatitude": 40.7128,
                "locationLongitude": -74.0060
            }
            """;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Helper method to create test tournament
    private static TournamentDetailsDTO createTestTournament() {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(TEST_TOURNAMENT_ID);
        dto.setName("Chess Masters 2024");
        return dto;
    }

    @Test
    void createTournament_ValidInput_ReturnSuccess() throws Exception {
        when(tournamentService.createTournament(any(TournamentRegistrationDTO.class), eq(TEST_USER_ID)))
                .thenReturn("Tournament created successfully");

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_TOURNAMENT_PAYLOAD)
                .header("X-User-Id", TEST_USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament created successfully"));

        verify(tournamentService).createTournament(any(TournamentRegistrationDTO.class), eq(TEST_USER_ID));
    }

    @Test
    void createTournament_MissingFields_ReturnBadRequest() throws Exception {
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

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("X-User-Id", TEST_USER_ID.toString()))
                .andExpect(status().isBadRequest());

        verify(tournamentService, times(0)).createTournament(any(TournamentRegistrationDTO.class), eq(TEST_USER_ID));
    }

    @Test
    void startTournament_ValidTournamentId_ReturnSuccess() throws Exception {
        when(tournamentService.startTournament(TEST_TOURNAMENT_ID)).thenReturn("Tournament started successfully");

        mockMvc.perform(post(BASE_URL + "/start/" + TEST_TOURNAMENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament started successfully"));

        verify(tournamentService).startTournament(TEST_TOURNAMENT_ID);
    }

    @Test
    void getTournament_ValidTournamentId_ReturnsTournamentDetails() throws Exception {
        when(tournamentService.getTournamentDetailsById(TEST_TOURNAMENT_ID)).thenReturn(TEST_TOURNAMENT);

        mockMvc.perform(get(BASE_URL + "/" + TEST_TOURNAMENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_TOURNAMENT_ID))
                .andExpect(jsonPath("$.name").value(TEST_TOURNAMENT.getName()));

        verify(tournamentService).getTournamentDetailsById(TEST_TOURNAMENT_ID);
    }

    @Test
    void getAllTournaments_Valid_ReturnTournaments() throws Exception {
        when(tournamentService.getAllTournaments()).thenReturn(List.of(TEST_TOURNAMENT));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_TOURNAMENT_ID))
                .andExpect(jsonPath("$[0].name").value(TEST_TOURNAMENT.getName()));

        verify(tournamentService).getAllTournaments();
    }

    @Test
    void updateTournament_ValidInputs_ReturnSuccess() throws Exception {
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

        mockMvc.perform(put(BASE_URL + "/" + TEST_TOURNAMENT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament updated successfully"));

        verify(tournamentService).updateTournament(eq(TEST_TOURNAMENT_ID), any(TournamentUpdateRequestDTO.class));
    }

    @Test
    void deleteTournament_ValidTournamentId_ReturnSuccess() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + TEST_TOURNAMENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully deleted tournament"));

        verify(tournamentService).deleteTournament(TEST_TOURNAMENT_ID);
    }

    @Test
    void updateCurrentRoundForTournament_ValidRoundId_ReturnSuccess() throws Exception {
        mockMvc.perform(put(BASE_URL + "/" + TEST_TOURNAMENT_ID + "/round/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Current round updated to 2"));

        verify(tournamentService).updateCurrentRoundForTournament(TEST_TOURNAMENT_ID, 2L);
    }

    @Test
    void completeTournament_ValidIds_ReturnSuccess() throws Exception {
        when(tournamentService.completeTournament(TEST_TOURNAMENT_ID, 2L)).thenReturn("Tournament has been completed");

        mockMvc.perform(put(BASE_URL + "/" + TEST_TOURNAMENT_ID + "/winner/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tournament has been completed"));

        verify(tournamentService).completeTournament(TEST_TOURNAMENT_ID, 2L);
    }

    @Test
    void getRegisteredTournaments_ValidPlayerId_ReturnTournaments() throws Exception {
        when(tournamentService.getRegisteredTournaments(TEST_PLAYER_ID)).thenReturn(List.of(TEST_TOURNAMENT));

        mockMvc.perform(get(BASE_URL + "/registered/" + TEST_PLAYER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_TOURNAMENT_ID))
                .andExpect(jsonPath("$[0].name").value(TEST_TOURNAMENT.getName()));

        verify(tournamentService).getRegisteredTournaments(TEST_PLAYER_ID);
    }

    @Test
    void getRegisteredTournamentsCurrentPlayer_ValidPlayerId_ReturnTournaments() throws Exception {
        when(tournamentService.getRegisteredTournaments(TEST_PLAYER_ID))
                .thenReturn(List.of(TEST_TOURNAMENT));

        mockMvc.perform(get(BASE_URL + "/registered/current")
                .header("X-User-PlayerId", TEST_PLAYER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(TEST_TOURNAMENT_ID))
                .andExpect(jsonPath("$[0].name").value(TEST_TOURNAMENT.getName()));

        verify(tournamentService).getRegisteredTournaments(TEST_PLAYER_ID);
    }

    @Test
    void getLiveTournamentsCurrent_ValidPlayerId_ReturnLiveTournaments() throws Exception {
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