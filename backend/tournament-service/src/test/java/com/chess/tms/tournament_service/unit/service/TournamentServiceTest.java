package com.chess.tms.tournament_service.unit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
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
class TournamentServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TournamentPlayerRepository tournamentPlayerRepository;
    @Mock private GameTypeRepository gameTypeRepository;
    @Mock private RoundTypeRepository roundTypeRepository;
    @Mock private RestTemplate restTemplate;

    @InjectMocks private TournamentService tournamentService;

    private Tournament tournament;
    private TournamentRegistrationDTO registrationDTO;
    private PlayerDetailsDTO playerDetails;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    @BeforeEach
    void setup() {
        registrationDTO = createRegistrationDTO();
        tournament = createTournament();
        playerDetails = createPlayerDetailsDTO();
    }

    @Test
    void createTournament_ValidInput_ReturnsSuccessMessage() {
        GameType gameType = new GameType();
        gameType.setId(1L);

        when(gameTypeRepository.getGameTypeById(1L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        String result = tournamentService.createTournament(registrationDTO, 1L);

        assertEquals("Tournament created successfully", result);
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void startTournament_ValidTournamentId_ReturnsSuccessMessage() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
                .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));
        when(roundTypeRepository.findById(1L)).thenReturn(Optional.of(new RoundType()));

        String result = tournamentService.startTournament(1L);

        assertTrue(result.contains("Test Tournament has started"));
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void startTournament_InvalidTournamentId_ThrowsException() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> tournamentService.startTournament(1L));
    }

    @Test
    void getTournamentDetailsById_ValidTournamentId_ReturnsDetails() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        TournamentDetailsDTO result = tournamentService.getTournamentDetailsById(1L);

        assertAll("Tournament Details",
            () -> assertNotNull(result),
            () -> assertEquals("Test Tournament", result.getName())
        );
    }

    @Test
    void registerPlayer_ValidPlayerId_IncrementsCurrentPlayers() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(restTemplate.getForEntity(playerServiceUrl + "/api/player/100", PlayerDetailsDTO.class))
                .thenReturn(new ResponseEntity<>(playerDetails, HttpStatus.OK));

        tournamentService.registerPlayer(100L, 1L);

        assertEquals(3, tournament.getCurrentPlayers());
        verify(tournamentPlayerRepository).save(any(TournamentPlayer.class));
    }

    @Test
    void completeTournament_ValidTournamentId_ReturnsSuccessMessage() {
        TournamentPlayer mockPlayer = new TournamentPlayer();
        mockPlayer.setPlayerId(1L);
        mockPlayer.setTournament(tournament);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(1L, 1L))
                .thenReturn(Optional.of(mockPlayer));

        String result = tournamentService.completeTournament(1L, 1L);

        assertEquals("Test Tournament has been completed", result);
        assertEquals(Status.COMPLETED, tournament.getStatus());
        verify(tournamentRepository).save(tournament);
    }

    @Test
    void updateTournament_AllFields_Success() {
        TournamentUpdateRequestDTO updateDTO = createTournamentUpdateRequestDTO();

        GameType gameType = new GameType();
        gameType.setId(2L);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(gameTypeRepository.getGameTypeById(2L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        tournamentService.updateTournament(1L, updateDTO);

        assertAll("Updated Tournament",
            () -> assertEquals(32, tournament.getMaxPlayers()),
            () -> assertEquals(1300, tournament.getMinElo()),
            () -> assertEquals(2200, tournament.getMaxElo()),
            () -> assertEquals(LocalDate.parse("2024-01-05"), tournament.getStartDate()),
            () -> assertEquals(LocalDate.parse("2024-02-01"), tournament.getEndDate()),
            () -> assertEquals(gameType, tournament.getTimeControl())
        );
        verify(tournamentRepository).save(tournament);
    }

    // Helper methods to initialize DTOs and models
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
        t.setStartDate(LocalDate.parse("2024-01-01"));
        t.setEndDate(LocalDate.parse("2024-01-02"));
        return t;
    }

    private TournamentRegistrationDTO createRegistrationDTO() {
        TournamentRegistrationDTO dto = new TournamentRegistrationDTO();
        dto.setName("Test Tournament");
        dto.setTimeControl(1);
        dto.setStartDate(LocalDate.parse("2024-01-01"));
        dto.setEndDate(LocalDate.parse("2024-01-02"));
        dto.setMaxPlayers(16);
        return dto;
    }

    private TournamentUpdateRequestDTO createTournamentUpdateRequestDTO() {
        TournamentUpdateRequestDTO dto = new TournamentUpdateRequestDTO();
        dto.setMaxPlayers(32);
        dto.setMinElo(1300);
        dto.setMaxElo(2200);
        dto.setStartDate(LocalDate.parse("2024-01-05"));
        dto.setEndDate(LocalDate.parse("2024-02-01"));
        dto.setTimeControl(2);
        return dto;
    }

    private PlayerDetailsDTO createPlayerDetailsDTO() {
        PlayerDetailsDTO dto = new PlayerDetailsDTO();
        dto.setEloRating(1500);
        return dto;
    }
}
