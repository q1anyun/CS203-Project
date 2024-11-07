package com.chess.tms.tournament_service.unit.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.tournament_service.dto.*;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.exception.*;
import com.chess.tms.tournament_service.model.*;
import com.chess.tms.tournament_service.repository.*;
import com.chess.tms.tournament_service.service.TournamentService;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentPlayerRepository tournamentPlayerRepository;

    @Mock
    private GameTypeRepository gameTypeRepository;

    @Mock
    private RoundTypeRepository roundTypeRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament tournament;
    private TournamentRegistrationDTO registrationDTO;

    @BeforeEach
    void setup() {
        registrationDTO = createRegistrationDTO();
        tournament = createTournament();
    }

    // Helper Methods to create DTOs and Entities
    private TournamentRegistrationDTO createRegistrationDTO() {
        TournamentRegistrationDTO dto = new TournamentRegistrationDTO();
        dto.setName("Test Tournament");
        dto.setTimeControl(1);
        dto.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        dto.setEndDate(LocalDateTime.parse("2024-01-02T00:00:00"));
        dto.setMaxPlayers(16);
        return dto;
    }

    private Tournament createTournament() {
        Tournament t = new Tournament();
        t.setTournamentId(1L);
        t.setName("Test Tournament");
        t.setStatus(Status.UPCOMING);
        t.setTimeControl(new GameType());
        t.setCurrentPlayers(2);
        t.setMaxPlayers(32);
        t.setMinElo(1000);
        t.setMaxElo(2000);
        t.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        t.setEndDate(LocalDateTime.parse("2024-01-02T00:00:00"));
        return t;
    }

    @Test
    void createTournament_ValidInput_Success() {
        GameType gameType = new GameType();
        gameType.setId(1L);

        when(gameTypeRepository.getGameTypeById(1L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        String result = tournamentService.createTournament(registrationDTO, 1L);

        assertEquals("Tournament created successfully", result);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void startTournament_ValidTournamentId_Success() {
        mockTournamentExistence();
        when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));
        when(roundTypeRepository.findById(1L)).thenReturn(Optional.of(new RoundType()));

        String result = tournamentService.startTournament(1L);

        assertTrue(result.contains("Test Tournament has started"));
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void startTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> tournamentService.startTournament(1L));
    }

    @Test
    void getTournamentDetailsById_ValidTournamentId_Success() {
        mockTournamentExistence();

        TournamentDetailsDTO result = tournamentService.getTournamentDetailsById(1L);

        assertNotNull(result);
        assertEquals("Test Tournament", result.getName());
    }

    @Test
    void getTournamentDetailsById_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> tournamentService.getTournamentDetailsById(1L));
    }

    @Test
    void registerPlayer_ValidPlayerId_Success() {
        mockTournamentExistence();

        tournamentService.registerPlayer(100L, 1L);

        verify(tournamentPlayerRepository).save(any(TournamentPlayer.class));
    }

    @Test
    void completeTournament_ValidTournamentId_Success() {
        TournamentPlayer mockPlayer = new TournamentPlayer();
        mockPlayer.setPlayerId(1L);
        mockPlayer.setTournament(tournament);

        when(tournamentRepository.findById(eq(1L))).thenReturn(Optional.of(tournament));
        when(tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(eq(1L), eq(1L)))
                .thenReturn(Optional.of(mockPlayer));

        String result = tournamentService.completeTournament(1L, 1L);

        assertEquals("Test Tournament has been completed", result);
        assertEquals(Status.COMPLETED, tournament.getStatus());
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void updateTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        TournamentUpdateRequestDTO updateDTO = createUpdateRequestDTO();

        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> tournamentService.updateTournament(99L, updateDTO));
    }

    @Test
    void updateTournament_PartialFields_Success() {
        TournamentUpdateRequestDTO updateDTO = createUpdateRequestDTO();

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        tournamentService.updateTournament(1L, updateDTO);

        assertEquals(20, tournament.getMaxPlayers());
        assertEquals(1200, tournament.getMinElo());
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void deleteTournament_ValidTournamentId_Success() {
        mockTournamentExistence();

        tournamentService.deleteTournament(1L);

        verify(tournamentRepository).deleteById(1L);
    }

    @Test
    void registerPlayer_InvalidTournamentPlayerId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> tournamentService.registerPlayer(100L, 1L));
    }

    @Test
    void startTournament_InsufficientPlayers_ThrowsInsufficientPlayersException() {
        tournament.setCurrentPlayers(1);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(InsufficientPlayersException.class, () -> tournamentService.startTournament(1L));
    }

    // Helper Method for Update Tournament DTO
    private TournamentUpdateRequestDTO createUpdateRequestDTO() {
        TournamentUpdateRequestDTO dto = new TournamentUpdateRequestDTO();
        dto.setMaxPlayers(20);
        dto.setMinElo(1200);
        return dto;
    }

    // Helper Method for Mocking Tournament Existence
    private void mockTournamentExistence() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    }
}
