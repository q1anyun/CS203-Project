package com.chess.tms.tournament_service.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.service.TournamentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(TournamentController.class)
public class TournamentControllerTest {

    @MockBean
    private TournamentService tournamentService;

    @Autowired
    private MockMvc mockMvc;

    private TournamentDetailsDTO tournamentDetails;

    @BeforeEach
    void setUp() {
        tournamentDetails = new TournamentDetailsDTO();
        tournamentDetails.setId(1L);
        tournamentDetails.setName("Test Tournament");
    }

    @Test
    public void testRegisterTournament() throws Exception {
        when(tournamentService.createTournament(any(), anyLong())).thenReturn(tournamentDetails);

        mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Test Tournament\", \"timeControl\": 1}")
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tournament"));
    }

    @Test
    public void testGetTournament() throws Exception {
        when(tournamentService.getTournamentDetailsById(anyLong())).thenReturn(tournamentDetails);

        mockMvc.perform(get("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tournament"));
    }

    @Test
    public void testDeleteTournament() throws Exception {
        when(tournamentService.deleteTournament(anyLong())).thenReturn(tournamentDetails);

        mockMvc.perform(delete("/api/tournaments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Tournament"));
    }

    @Test
    public void testUpdateTournament() throws Exception {
        mockMvc.perform(put("/api/tournaments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Tournament\", \"timeControl\": 1}"))
                .andExpect(status().isOk());
    }
}