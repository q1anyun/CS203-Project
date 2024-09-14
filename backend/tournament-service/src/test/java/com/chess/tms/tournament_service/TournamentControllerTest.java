package com.chess.tms.tournament_service;

import com.chess.tms.tournament_service.controller.TournamentController;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationRequestDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateDTO;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.enums.Type;
import com.chess.tms.tournament_service.service.TournamentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(TournamentController.class)
public class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;  // To perform simulated HTTP requests

    @MockBean
    private TournamentService tournamentService;  // Mock the service layer

    @Autowired
    private ObjectMapper objectMapper;  // To convert objects to/from JSON

    private TournamentRegistrationRequestDTO registrationRequestDTO;
    private TournamentDetailsDTO tournamentDetailsDTO;
    private TournamentUpdateDTO tournamentUpdateDTO;

    @BeforeEach
    void setUp() {
        // Initialize test data for the DTOs
        registrationRequestDTO = new TournamentRegistrationRequestDTO(
            1,
            "Chess Tournament",
            LocalDateTime.of(2024, 9, 10, 10, 0),
            LocalDateTime.of(2024, 9, 15, 18, 0),
            1200,
            1800,
            16,
            8,
            "Virtual",
            Status.UPCOMING,
            Type.VIRTUAL
        );

        tournamentDetailsDTO = new TournamentDetailsDTO(
            "Chess Tournament",
            LocalDateTime.of(2024, 9, 10, 10, 0),
            LocalDateTime.of(2024, 9, 15, 18, 0),
            1200,
            1800,
            16,
            8,
            "Virtual",
            Status.UPCOMING,
            Type.VIRTUAL
        );

        tournamentUpdateDTO = new TournamentUpdateDTO(
            tournamentDetailsDTO,  // Old tournament details
            new TournamentDetailsDTO(
                "Updated Chess Tournament",
                LocalDateTime.of(2024, 10, 1, 10, 0),
                LocalDateTime.of(2024, 10, 10, 18, 0),
                1400,
                2000,
                20,
                10,
                "Physical",
                Status.IN_PROGRESS,
                Type.PHYSICAL
            )
        );
    }

    @Test
    void testRegisterTournament() throws Exception {
        // Mock the service to return tournamentDetailsDTO when createTournament is called
        when(tournamentService.createTournament(any(TournamentRegistrationRequestDTO.class)))
                .thenReturn(tournamentDetailsDTO);

        // Perform POST request to register a new tournament
        ResultActions result = mockMvc.perform(post("/api/tournaments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequestDTO))
                .with(csrf())
                        .with(user("user").roles("USER")));
                // Assert the response status is OK and the content matches the tournamentDetailsDTO
        result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Chess Tournament"))
        .andExpect(jsonPath("$.minElo").value(1200))
        .andExpect(jsonPath("$.status").value("UPCOMING"))
        .andExpect(jsonPath("$.type").value("VIRTUAL"));
}

@Test
void testGetTournament() throws Exception {
  // Mock the service to return tournamentDetailsDTO when getTournamentDetailsById is called
  when(tournamentService.getTournamentDetailsById(eq(1L))).thenReturn(tournamentDetailsDTO);

  // Perform GET request to fetch tournament details by ID
  ResultActions result = mockMvc.perform(get("/api/tournaments/1")
          .contentType(MediaType.APPLICATION_JSON)
          .with(csrf())
          .with(user("user").roles("USER")));

  // Assert the response is OK and the content matches tournamentDetailsDTO
  result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Chess Tournament"))
        .andExpect(jsonPath("$.minElo").value(1200))
        .andExpect(jsonPath("$.status").value("UPCOMING"));
}

// @Test
// void testUpdateTournament() throws Exception {
//   // Mock the service to return updated tournament details
//   when(tournamentService.updateTournament(eq(1L), any(TournamentDetailsDTO.class))).thenReturn(tournamentUpdateDTO.getNewTournament());

//   // Perform PUT request to update a tournament by ID
//   ResultActions result = mockMvc.perform(put("/api/tournaments/1")
//           .contentType(MediaType.APPLICATION_JSON)
//           .content(objectMapper.writeValueAsString(tournamentDetailsDTO)));

//   // Assert the response status is OK and the content matches the updated tournament details
//   result.andExpect(status().isOk())
//         .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//         .andExpect(jsonPath("$.name").value("Updated Chess Tournament"))
//         .andExpect(jsonPath("$.minElo").value(1400))
//         .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
//         .andExpect(jsonPath("$.type").value("PHYSICAL"));
// }

@Test
void testDeleteTournament() throws Exception {
  // Mock the service to return tournamentDetailsDTO when deleteTournament is called
  when(tournamentService.deleteTournament(eq(1))).thenReturn(tournamentDetailsDTO);

  // Perform DELETE request to delete a tournament by ID
  ResultActions result = mockMvc.perform(delete("/api/tournaments")
          .param("id", "1")
          .contentType(MediaType.APPLICATION_JSON)
          .with(csrf())
          .with(user("user").roles("USER")));

  // Assert the response status is OK and the content matches the tournamentDetailsDTO
  result.andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.name").value("Chess Tournament"))
        .andExpect(jsonPath("$.status").value("UPCOMING"));
}
}