package com.chess.tms.tournament_service.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
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

import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.exception.GameTypeNotFoundException;
import com.chess.tms.tournament_service.exception.InsufficientPlayersException;
import com.chess.tms.tournament_service.exception.PlayerAlreadyRegisteredException;
import com.chess.tms.tournament_service.exception.RoundTypeNotFoundException;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.exception.UserDoesNotExistException;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.repository.RoundTypeRepository;
import com.chess.tms.tournament_service.repository.TournamentPlayerRepository;
import com.chess.tms.tournament_service.repository.TournamentRepository;
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
        registrationDTO = new TournamentRegistrationDTO();
        registrationDTO.setName("Test Tournament");
        registrationDTO.setTimeControl(1);
        registrationDTO.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        registrationDTO.setEndDate(LocalDateTime.parse("2024-01-02T00:00:00"));
        registrationDTO.setMaxPlayers(16);

        tournament = new Tournament();
        tournament.setTournamentId(1L);
        tournament.setName("Test Tournament");
        tournament.setStatus(Status.UPCOMING);
        tournament.setTimeControl(new GameType());
        tournament.setCurrentPlayers(2);
        tournament.setMaxPlayers(32);
        tournament.setMinElo(1000);
        tournament.setMaxElo(2000);
        tournament.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        tournament.setEndDate(LocalDateTime.parse("2024-01-02T00:00:00"));
    }

    @Test
    void createTournament_ValidInput_Success() {
        GameType gameType = new GameType();
        gameType.setId(1L);
        when(gameTypeRepository.getGameTypeById(1L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        String result = tournamentService.createTournament(registrationDTO, 1L);

        assertEquals("Tournament created successfully", result);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void startTournament_ValidTournamentId_Success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));
        when(roundTypeRepository.findById(1L)).thenReturn(Optional.of(new RoundType()));

        String result = tournamentService.startTournament(1L);

        assertTrue(result.contains("Test Tournament has started"));
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void startTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.startTournament(1L);
        });
    }

    @Test
    void getTournamentDetailsById_ValidTournamentId_Success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        TournamentDetailsDTO result = tournamentService.getTournamentDetailsById(1L);

        assertNotNull(result);
        assertEquals("Test Tournament", result.getName());
    }

    @Test
    void getTournamentDetailsById_ValidWinnerId_Success() {
        Tournament tournament = new Tournament();
        tournament.setTournamentId(1L);
        tournament.setName("Test Tournament");
        tournament.setWinnerId(101L);

        PlayerDetailsDTO winnerDetails = new PlayerDetailsDTO();
        winnerDetails.setId(101L);
        winnerDetails.setFirstName("John");
        winnerDetails.setLastName("Doe");

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        when(restTemplate.getForEntity(
                anyString(),
                eq(PlayerDetailsDTO.class)))
                .thenReturn(new ResponseEntity<>(winnerDetails, HttpStatus.OK));

        TournamentDetailsDTO result = tournamentService.getTournamentDetailsById(1L);

        assertNotNull(result);
        assertEquals("Test Tournament", result.getName());
        assertNotNull(result.getWinner());
        assertEquals(101L, result.getWinner().getId());
        assertEquals("John", result.getWinner().getFirstName());
        assertEquals("Doe", result.getWinner().getLastName());

        verify(tournamentRepository, times(1)).findById(1L);

        verify(restTemplate, times(1)).getForEntity(
                anyString(),
                eq(PlayerDetailsDTO.class));
    }

    @Test
    void getTournamentDetailsById_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.getTournamentDetailsById(1L);
        });
    }

    @Test
    void registerPlayer_ValidPlayerId_Success() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.registerPlayer(100L, 1L);

        verify(tournamentPlayerRepository, times(1)).save(any(TournamentPlayer.class));
    }

    @Test
    void completeTournament_ValidTournamentTId_Success() {
        TournamentPlayer mockPlayer = new TournamentPlayer();
        mockPlayer.setPlayerId(1L);
        mockPlayer.setTournament(tournament);

        when(tournamentRepository.findById(eq(1L))).thenReturn(Optional.of(tournament));
        when(tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(eq(1L), eq(1L)))
                .thenReturn(Optional.of(mockPlayer));

        String result = tournamentService.completeTournament(1L, 1L);

        assertEquals("Test Tournament has been completed", result);
        assertEquals(Status.COMPLETED, tournament.getStatus());

        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void completeTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.completeTournament(1L, 100L);
        });

        verify(tournamentPlayerRepository, never()).findByPlayerIdAndTournament_TournamentId(anyLong(), anyLong());
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void updateTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        TournamentUpdateRequestDTO updateDTO = new TournamentUpdateRequestDTO();
        updateDTO.setMaxPlayers(32);
        updateDTO.setMinElo(1200);

        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.updateTournament(99L, updateDTO);
        });

        verify(tournamentRepository, times(0)).save(any(Tournament.class));
    }

    @Test
    void updateTournament_PartialFields_Success() {
        TournamentUpdateRequestDTO updateDTO = new TournamentUpdateRequestDTO();
        updateDTO.setMaxPlayers(20);
        updateDTO.setMinElo(1200);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        tournamentService.updateTournament(1L, updateDTO);

        assertEquals(20, tournament.getMaxPlayers());
        assertEquals(1200, tournament.getMinElo());
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void updateTournament_AllFields_Success() {
        TournamentUpdateRequestDTO updateDTO = new TournamentUpdateRequestDTO();
        updateDTO.setMaxPlayers(32);
        updateDTO.setMinElo(1300);
        updateDTO.setMaxElo(2200);
        updateDTO.setStartDate(LocalDateTime.parse("2024-01-05T00:00:00"));
        updateDTO.setEndDate(LocalDateTime.parse("2024-02-01T00:00:00"));
        updateDTO.setTimeControl(2);

        GameType gameType = new GameType();
        gameType.setId(2L);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(gameTypeRepository.getGameTypeById(2L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        tournamentService.updateTournament(1L, updateDTO);

        assertEquals(32, tournament.getMaxPlayers());
        assertEquals(1300, tournament.getMinElo());
        assertEquals(2200, tournament.getMaxElo());
        assertEquals(LocalDateTime.parse("2024-01-05T00:00:00"), tournament.getStartDate());
        assertEquals(LocalDateTime.parse("2024-02-01T00:00:00"), tournament.getEndDate());
        assertEquals(gameType, tournament.getTimeControl());
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void deleteTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.deleteTournament(99L);
        });

        verify(tournamentRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void deleteTournament_ValidTournamentId_Success() {
        Tournament tournament = new Tournament();
        tournament.setTournamentId(1L);
        tournament.setName("Test Tournament");

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.deleteTournament(1L);

        verify(tournamentRepository, times(1)).deleteById(1L);
    }

    @Test
    void registerPlayer_InvalidTournamentPlayerId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.registerPlayer(100L, 1L);
        });

        verify(tournamentPlayerRepository, times(0)).save(any(TournamentPlayer.class));
    }

    @Test
    void deletePlayerFromTournament_ValidTournamentPlayer_Success() {
        TournamentPlayer tournamentPlayer = new TournamentPlayer();
        tournamentPlayer.setId(1L);
        tournamentPlayer.setPlayerId(100L);
        tournamentPlayer.setTournament(tournament);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentPlayerRepository.findById(1L)).thenReturn(Optional.of(tournamentPlayer));

        tournamentService.deletePlayerFromTournament(1L, 1L);

        verify(tournamentPlayerRepository, times(1)).deleteByPlayerId(1L);
    }

    @Test
    void deletePlayerFromTournament_InvalidTournamentPlayerId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.deletePlayerFromTournament(1L, 1L);
        });
        verify(tournamentPlayerRepository, times(0)).deleteByPlayerId(anyLong());
    }

    @Test
    void getPlayersByTournament_InvalidTournamentId_ThrowsTournamentDoesNotExistException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.getPlayersByTournament(1L);
        });

        verify(tournamentPlayerRepository, times(0)).findAllByTournament(any(Tournament.class));
        verify(restTemplate, times(0)).postForEntity(anyString(), any(), eq(PlayerDetailsDTO[].class));
    }

    @Test
    void startTournament_InsufficientPlayers_ThrowsInsufficientPlayersException() {
        tournament.setCurrentPlayers(1);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(InsufficientPlayersException.class, () -> {
            tournamentService.startTournament(1L);
        });

        verify(restTemplate, times(0)).postForEntity(anyString(), any(), any());
        verify(tournamentRepository, times(0)).save(any(Tournament.class));
    }

}