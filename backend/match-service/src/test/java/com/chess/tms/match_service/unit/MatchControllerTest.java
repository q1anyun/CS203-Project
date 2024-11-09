package com.chess.tms.match_service.unit;
import com.chess.tms.match_service.controller.MatchController;
import com.chess.tms.match_service.dto.*;
import com.chess.tms.match_service.service.MatchService;
import com.chess.tms.match_service.exception.GameTypeNotFoundException;
import com.chess.tms.match_service.exception.GlobalExceptionHandler;
import com.chess.tms.match_service.exception.MatchAlreadyCompletedException;
import com.chess.tms.match_service.exception.MatchDoesNotExistException;
import com.chess.tms.match_service.exception.PlayerDoesNotExistInMatchException;
import com.chess.tms.match_service.exception.RoundTypeNotFoundException;
import com.chess.tms.match_service.exception.SwissBracketNotFoundException;
import com.chess.tms.match_service.model.RoundType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private MatchService matchService;

    @InjectMocks
    private MatchController matchController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(matchController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createKnockoutMatches_ShouldReturnRoundTypeId() throws Exception {
        // Arrange
        Long tournamentId = 1L;
        Long gameTypeId = 1L;
        Long roundTypeId = 1L;
        
        when(matchService.createKnockoutMatches(eq(tournamentId), eq(gameTypeId), any()))
            .thenReturn(roundTypeId);
    
        // Act & Assert
        mockMvc.perform(post("/api/matches/tournament/{tournamentId}/knockout//{gameTypeId}", tournamentId, gameTypeId)  // Use explicit values instead of path variables
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(roundTypeId.toString()));
    
        verify(matchService).createKnockoutMatches(tournamentId, gameTypeId, null);
    }

    @Test
    void createKnockoutMatches_WithInvalidNumberOfPlayers_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long tournamentId = 1L;
        Long gameTypeId = 1L;

        int invalidNumberOfPlayers = 80; 
        
        when(matchService.createKnockoutMatches(eq(tournamentId), eq(gameTypeId), any()))
            .thenThrow(new RoundTypeNotFoundException( "Round type not found for " + invalidNumberOfPlayers + " players"));
    
        // Act & Assert
        mockMvc.perform(post("/api/matches/tournament/{tournamentId}/knockout//{gameTypeId}", tournamentId, gameTypeId))
                .andExpect(status().isNotFound());
    
        verify(matchService).createKnockoutMatches(tournamentId, gameTypeId, null);
    }


    @Test
    void createKnockoutMatches_WithInvalidRoundTypeId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long tournamentId = 1L;
        Long invalidGameTypeId = 1L;
        
        when(matchService.createKnockoutMatches(eq(tournamentId), eq(invalidGameTypeId), any()))
            .thenThrow(new GameTypeNotFoundException("GameType with ID " + invalidGameTypeId + " not found"));
    
        // Act & Assert
        mockMvc.perform(post("/api/matches/tournament/{tournamentId}/knockout/{gameTypeId}", tournamentId, invalidGameTypeId) ) 
                .andExpect(status().isNotFound()); 
    
        verify(matchService).createKnockoutMatches(tournamentId, invalidGameTypeId, null);
    }

    @Test
    void getMatchesByTournament_ShouldReturnMatchList() throws Exception {
        // Arrange
        Long tournamentId = 1L;
        MatchDTO match1 = new MatchDTO();
        match1.setId(1L);
        match1.setRoundType(new RoundType());
        
        MatchDTO match2 = new MatchDTO();
        match2.setId(2L);
        match2.setRoundType(new RoundType());
        
        List<MatchDTO> expectedMatches = Arrays.asList(match1, match2);
        when(matchService.getMatchesByTournament(tournamentId)).thenReturn(expectedMatches);

        // Act & Assert
        mockMvc.perform(get("/api/matches/tournament/{tournamentId}", tournamentId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedMatches)));

        verify(matchService).getMatchesByTournament(tournamentId);
    }

    @Test
    void getMatch_ShouldReturnMatch() throws Exception {
        // Arrange
        Long matchId = 1L;
        MatchDTO expectedMatch = new MatchDTO();
        expectedMatch.setId(matchId);
        expectedMatch.setRoundType(new RoundType());
        when(matchService.getMatch(matchId)).thenReturn(expectedMatch);

        // Act & Assert
        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedMatch)));

        verify(matchService).getMatch(matchId);
    }

    @Test
    void updateMatchResult_WithValidWinner_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long matchId = 1L;
        Long winnerId = 2L;
        String expectedMessage = "Winner advanced successfully";
        when(matchService.advanceWinner(matchId, winnerId)).thenReturn(expectedMessage);

        // Act & Assert
        mockMvc.perform(put("/api/matches/{matchId}/winner/{winnerId}", matchId, winnerId))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        verify(matchService).advanceWinner(matchId, winnerId);
    }

    @Test
    void getRecentMatchesByPlayer_WithValidPlayerId_ShouldReturnMatches() throws Exception {
        // Arrange
        Long playerId = 1L;
        MatchDTO match1 = new MatchDTO();
        match1.setId(1L);
        match1.setRoundType(new RoundType());
        
        MatchDTO match2 = new MatchDTO();
        match2.setId(2L);
        match2.setRoundType(new RoundType());
        
        List<MatchDTO> expectedMatches = Arrays.asList(match1, match2);
        when(matchService.getRecentMatchesByPlayerId(playerId)).thenReturn(expectedMatches);

        // Act & Assert
        mockMvc.perform(get("/api/matches/player/{playerId}/recent", playerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedMatches)));

        verify(matchService).getRecentMatchesByPlayerId(playerId);
    }

    @Test
    void getRecentMatchesByPlayer_WithHeader_ShouldReturnMatches() throws Exception {
        // Arrange
        String playerIdStr = "1";
        Long playerId = 1L;
        MatchDTO match1 = new MatchDTO();
        match1.setId(1L);
        MatchDTO match2 = new MatchDTO();
        match2.setId(2L);
        List<MatchDTO> expectedMatches = Arrays.asList(match1, match2);
        when(matchService.getRecentMatchesByPlayerId(playerId)).thenReturn(expectedMatches);

        // Act & Assert
        mockMvc.perform(get("/api/matches/player/current/recent")
                .header("X-User-PlayerId", playerIdStr))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedMatches)));

        verify(matchService).getRecentMatchesByPlayerId(playerId);
    }

    @Test
    void getMatch_WithInvalidId_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long matchId = 999L;
        when(matchService.getMatch(matchId)).thenThrow(new MatchDoesNotExistException("Match not found"));

        // Act & Assert
        mockMvc.perform(get("/api/matches/{matchId}", matchId))
                .andExpect(status().isNotFound());

        verify(matchService).getMatch(matchId);
    }

    @Test
    void updateMatchResult_WithInvalidWinner_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long matchId = 1L;
        Long winnerId = 999L;
        when(matchService.advanceWinner(matchId, winnerId))
            .thenThrow(new PlayerDoesNotExistInMatchException("Player with id " + winnerId + " is not recognised in the match."));

        // Act & Assert
        mockMvc.perform(put("/api/matches/{matchId}/winner/{winnerId}", matchId, winnerId))
                .andExpect(status().isNotFound());

        verify(matchService).advanceWinner(matchId, winnerId);
    }

    @Test
    void updateMatchResult_WithInvalidSwissBracket_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long matchId = 1L; //match with tournament that does not have valid swiss id 
        Long winnerId = 1L;
        when(matchService.advanceWinner(matchId, winnerId))
            .thenThrow(new SwissBracketNotFoundException("Swiss bracket not found")); 
        // Act & Assert
        mockMvc.perform(put("/api/matches/{matchId}/winner/{winnerId}", matchId, winnerId))
                .andExpect(status().isNotFound());

        verify(matchService).advanceWinner(matchId, winnerId);
    }

    @Test
    void updateMatchResult_WithCompletedMatch_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long matchId = 1L;
        Long winnerId = 1L;
        when(matchService.advanceWinner(matchId, winnerId))
            .thenThrow(new MatchAlreadyCompletedException("Match has already been completed"));

        // Act & Assert
        mockMvc.perform(put("/api/matches/{matchId}/winner/{winnerId}", matchId, winnerId))
                .andExpect(status().isBadRequest());

        verify(matchService).advanceWinner(matchId, winnerId);
    }

    @Test
    void createSwissMatches_ShouldReturnBracketId() throws Exception {
        // Arrange
        Long tournamentId = 1L;
        Long gameTypeId = 1L;
        Long bracketId = 1L; 
        when(matchService.createSwissMatches(tournamentId, gameTypeId))
            .thenReturn(bracketId);

        // Act & Assert
        mockMvc.perform(post("/api/matches/tournament/{tournamentId}/swiss/{gameTypeId}", tournamentId, gameTypeId))
                .andExpect(status().isOk());

        verify(matchService).createSwissMatches(tournamentId, gameTypeId);
    }
}