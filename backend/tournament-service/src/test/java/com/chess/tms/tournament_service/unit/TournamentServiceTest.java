package com.chess.tms.tournament_service.unit;

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
        tournament.setCurrentPlayers(0);
        tournament.setMaxPlayers(32);
        tournament.setMinElo(1000);
        tournament.setMaxElo(2000);
        tournament.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        tournament.setEndDate(LocalDateTime.parse("2024-01-02T00:00:00"));
    }

    @Test
    void testCreateTournament() {
        GameType gameType = new GameType();
        gameType.setId(1L);
        when(gameTypeRepository.getGameTypeById(1L)).thenReturn(Optional.of(gameType));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);
    
        String result = tournamentService.createTournament(registrationDTO, 1L);
    
        assertEquals("Tournament created successfully", result);
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    void testStartTournamentSuccess() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
            .thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));
        when(roundTypeRepository.findById(1L)).thenReturn(Optional.of(new RoundType()));

        String result = tournamentService.startTournament(1L);

        assertTrue(result.contains("Test Tournament has started"));
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
    void testStartTournamentNotFound() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.startTournament(1L);
        });
    }

    @Test
    void testGetTournamentDetailsByIdSuccess() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        TournamentDetailsDTO result = tournamentService.getTournamentDetailsById(1L);

        assertNotNull(result);
        assertEquals("Test Tournament", result.getName());
    }

    @Test
void testGetTournamentDetailsByIdWithWinner() {
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
    void testGetTournamentDetailsByIdNotFound() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.getTournamentDetailsById(1L);
        });
    }

    @Test
    void testRegisterPlayer() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        tournamentService.registerPlayer(100L, 1L);

        verify(tournamentPlayerRepository, times(1)).save(any(TournamentPlayer.class));
    }

    @Test
    void testCompleteTournamentSuccess() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(tournamentPlayerRepository.existsByPlayerIdAndTournamentId(1L, 1L)).thenReturn(true);

        String result = tournamentService.completeTournament(1L, 1L);

        assertEquals("Test Tournament has been completed", result);
        assertEquals(Status.COMPLETED, tournament.getStatus());
        verify(tournamentRepository, times(1)).save(tournament);
    }

    @Test
void testCompleteTournamentWhenTournamentDoesNotExist() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.completeTournament(1L, 100L);
    });

    verify(tournamentPlayerRepository, never()).existsByPlayerIdAndTournamentId(anyLong(), anyLong());
    verify(tournamentRepository, never()).save(any(Tournament.class));
}

    @Test
void testUpdateTournamentNonExistent() {
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
void testUpdateTournamentPartialFields() {
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
void testUpdateTournamentAllFields() {
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
void testDeleteTournamentNotFound() {
    when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.deleteTournament(99L);
    });

    verify(tournamentRepository, times(0)).deleteById(anyLong());
}

@Test
void testDeleteTournamentSuccess() {
    Tournament tournament = new Tournament();
    tournament.setTournamentId(1L);
    tournament.setName("Test Tournament");

    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

    tournamentService.deleteTournament(1L);

    verify(tournamentRepository, times(1)).deleteById(1L);
}

@Test
void testRegisterPlayerTournamentNotFound() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.registerPlayer(100L, 1L);
    });

    verify(tournamentPlayerRepository, times(0)).save(any(TournamentPlayer.class));
}

@Test
void testDeletePlayerFromTournament() {
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
void testGetLiveTournamentsForPlayer() {
    TournamentPlayer tournamentPlayer1 = new TournamentPlayer();
    tournamentPlayer1.setTournament(tournament);
    
    Tournament liveTournament = new Tournament();
    liveTournament.setTournamentId(2L);
    liveTournament.setName("Live Tournament");
    liveTournament.setStatus(Status.LIVE);

    TournamentPlayer tournamentPlayer2 = new TournamentPlayer();
    tournamentPlayer2.setTournament(liveTournament);

    when(tournamentPlayerRepository.findAllByPlayerId(100L))
        .thenReturn(Arrays.asList(tournamentPlayer1, tournamentPlayer2));

    List<TournamentDetailsDTO> liveTournaments = tournamentService.getLiveTournaments(100L);

    assertEquals(1, liveTournaments.size());
    assertEquals("Live Tournament", liveTournaments.get(0).getName());
}

@Test
void testStartTournamentRoundTypeNotFound() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
        .thenReturn(new ResponseEntity<>(99L, HttpStatus.OK));
    when(roundTypeRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(RoundTypeNotFoundException.class, () -> {
        tournamentService.startTournament(1L);
    });
}

@Test
void testStartTournamentAlreadyLive() {
    tournament.setStatus(Status.LIVE);
    
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

    ResponseEntity<Long> responseEntity = new ResponseEntity<>(1L, HttpStatus.OK);
    when(restTemplate.postForEntity(anyString(), eq(null), eq(Long.class)))
            .thenReturn(responseEntity);

    RoundType roundType = new RoundType();
    roundType.setId(1L);
    roundType.setRoundName("Round 1");
    when(roundTypeRepository.findById(1L)).thenReturn(Optional.of(roundType));

    String result = tournamentService.startTournament(1L);

    assertTrue(result.contains("Test Tournament has started and current round is Round 1"));
    assertEquals(Status.LIVE, tournament.getStatus());
    verify(tournamentRepository, times(1)).save(tournament);
}

@Test
void testUpdateTournamentInvalidGameType() {
    TournamentUpdateRequestDTO updateDTO = new TournamentUpdateRequestDTO();
    updateDTO.setTimeControl(999);

    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(gameTypeRepository.getGameTypeById(999)).thenReturn(Optional.empty());

    assertThrows(GameTypeNotFoundException.class, () -> {
        tournamentService.updateTournament(1L, updateDTO);
    });
}

@Test
void testRegisterPlayerToFullTournament() {
    tournament.setMaxPlayers(2);
    tournament.setCurrentPlayers(2);
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

    assertThrows(RuntimeException.class, () -> {
        tournamentService.registerPlayer(101L, 1L);
    });

    verify(tournamentPlayerRepository, times(0)).save(any(TournamentPlayer.class));
}

@Test
void testRegisterPlayerAlreadyRegistered() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    
    when(tournamentPlayerRepository.existsByPlayerIdAndTournamentId(100L, 1L)).thenReturn(true);

    assertThrows(PlayerAlreadyRegisteredException.class, () -> {
        tournamentService.registerPlayer(100L, 1L);
    });

    verify(tournamentPlayerRepository, times(0)).save(any(TournamentPlayer.class));
    verify(tournamentRepository, times(0)).save(any(Tournament.class));
}

@Test
void testDeletePlayerNotFound() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(tournamentPlayerRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(UserDoesNotExistException.class, () -> {
        tournamentService.deletePlayerFromTournament(99L, 1L);
    });

    verify(tournamentPlayerRepository, times(0)).deleteByPlayerId(anyLong());
}

@Test
void testGetAllTournamentsWhenEmpty() {
    when(tournamentRepository.findAll()).thenReturn(new ArrayList<>());

    List<TournamentDetailsDTO> tournaments = tournamentService.getAllTournaments();

    assertTrue(tournaments.isEmpty());
}

@Test
void testGetAllTournamentsNonEmpty() {
    Tournament tournament1 = new Tournament();
    tournament1.setTournamentId(1L);
    tournament1.setName("Tournament 1");

    Tournament tournament2 = new Tournament();
    tournament2.setTournamentId(2L);
    tournament2.setName("Tournament 2");

    List<Tournament> mockTournaments = Arrays.asList(tournament1, tournament2);

    when(tournamentRepository.findAll()).thenReturn(mockTournaments);

    List<TournamentDetailsDTO> result = tournamentService.getAllTournaments();

    assertEquals(2, result.size());
    assertEquals("Tournament 1", result.get(0).getName());
    assertEquals(1L, result.get(0).getId());
    assertEquals("Tournament 2", result.get(1).getName());
    assertEquals(2L, result.get(1).getId());

    verify(tournamentRepository, times(1)).findAll();
}

@Test
void testGetRegisteredTournamentsNoRegistrations() {
    when(tournamentPlayerRepository.findAllByPlayerId(100L)).thenReturn(new ArrayList<>());

    List<TournamentDetailsDTO> tournaments = tournamentService.getRegisteredTournaments(100L);

    assertTrue(tournaments.isEmpty());
}

@Test
void testGetRegisteredTournamentsWithRegistrations() {
    TournamentPlayer tournamentPlayer1 = new TournamentPlayer();
    tournamentPlayer1.setId(1L);
    tournamentPlayer1.setPlayerId(100L);
    Tournament tournament1 = new Tournament();
    tournament1.setTournamentId(10L);
    tournament1.setName("Chess Championship");
    tournamentPlayer1.setTournament(tournament1);

    TournamentPlayer tournamentPlayer2 = new TournamentPlayer();
    tournamentPlayer2.setId(2L);
    tournamentPlayer2.setPlayerId(100L);
    Tournament tournament2 = new Tournament();
    tournament2.setTournamentId(20L);
    tournament2.setName("Grand Masters Tournament");
    tournamentPlayer2.setTournament(tournament2);

    when(tournamentPlayerRepository.findAllByPlayerId(100L))
        .thenReturn(Arrays.asList(tournamentPlayer1, tournamentPlayer2));

    List<TournamentDetailsDTO> registeredTournaments = tournamentService.getRegisteredTournaments(100L);

    assertEquals(2, registeredTournaments.size());
    assertEquals(10L, registeredTournaments.get(0).getId());
    assertEquals("Chess Championship", registeredTournaments.get(0).getName());
    assertEquals(20L, registeredTournaments.get(1).getId());
    assertEquals("Grand Masters Tournament", registeredTournaments.get(1).getName());
}

@Test
void testGetPlayersByTournamentNoPlayers() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(tournamentPlayerRepository.findAllByTournament(tournament)).thenReturn(new ArrayList<>());
    
    PlayerDetailsDTO[] emptyPlayerDetailsArray = new PlayerDetailsDTO[0];
    when(restTemplate.postForEntity(
        anyString(),
        any(),
        eq(PlayerDetailsDTO[].class))
    ).thenReturn(new ResponseEntity<>(emptyPlayerDetailsArray, HttpStatus.OK));

    List<PlayerDetailsDTO> players = tournamentService.getPlayersByTournament(1L);

    assertTrue(players.isEmpty());
}

@Test
void testCompleteTournamentNoWinner() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(tournamentPlayerRepository.existsByPlayerIdAndTournamentId(99L, 1L)).thenReturn(false);

    assertThrows(UserDoesNotExistException.class, () -> {
        tournamentService.completeTournament(1L, 99L);
    });

    verify(tournamentRepository, times(0)).save(any(Tournament.class));
}

@Test
void testUpdateCurrentRoundForNonExistentTournament() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.updateCurrentRoundForTournament(1L, 2L);
    });
}

@Test
void testUpdateCurrentRoundForNonExistentRoundType() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

    when(roundTypeRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(RoundTypeNotFoundException.class, () -> {
        tournamentService.updateCurrentRoundForTournament(1L, 2L);
    });

    verify(tournamentRepository, never()).save(any(Tournament.class));
}

@Test
void testUpdateCurrentRoundForTournamentSuccess() {
    Tournament tournament = new Tournament();
    tournament.setTournamentId(1L);
    RoundType newRoundType = new RoundType();
    newRoundType.setId(2L);
    newRoundType.setRoundName("Round 2");

    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    when(roundTypeRepository.findById(2L)).thenReturn(Optional.of(newRoundType));

    tournamentService.updateCurrentRoundForTournament(1L, 2L);

    assertEquals(newRoundType, tournament.getCurrentRound());
    verify(tournamentRepository, times(1)).save(tournament);
}

@Test
void testGetPlayersByTournamentWithPlayers() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
    TournamentPlayer player1 = new TournamentPlayer();
    player1.setPlayerId(101L);
    TournamentPlayer player2 = new TournamentPlayer();
    player2.setPlayerId(102L);
    when(tournamentPlayerRepository.findAllByTournament(tournament))
        .thenReturn(Arrays.asList(player1, player2));

    PlayerDetailsDTO[] playerDetailsArray = {
        new PlayerDetailsDTO(101L, 101L, 1500, "Player", "One", "https://example.com/p1.jpg", 20, 5, 25, 1600, 1400, "USA"),
        new PlayerDetailsDTO(102L, 102L, 1400, "Player", "Two", "https://example.com/p2.jpg", 15, 10, 25, 1500, 1300, "UK")
    };
    when(restTemplate.postForEntity(
        anyString(),
        any(),
        eq(PlayerDetailsDTO[].class))
    ).thenReturn(new ResponseEntity<>(playerDetailsArray, HttpStatus.OK));

    List<PlayerDetailsDTO> players = tournamentService.getPlayersByTournament(1L);

    assertEquals(2, players.size());
    assertEquals(101L, players.get(0).getId());
    assertEquals("Player", players.get(0).getFirstName());
    assertEquals("One", players.get(0).getLastName());
    assertEquals(1500, players.get(0).getEloRating());
    assertEquals(102L, players.get(1).getId());
    assertEquals("Player", players.get(1).getFirstName());
    assertEquals("Two", players.get(1).getLastName());
    assertEquals(1400, players.get(1).getEloRating());
}

@Test
void testDeletePlayerFromTournamentSuccess() {
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
void testDeletePlayerFromTournament_TournamentDoesNotExist() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.deletePlayerFromTournament(1L, 1L);
    });
    verify(tournamentPlayerRepository, times(0)).deleteByPlayerId(anyLong());
}

@Test
void testGetPlayersByTournament_TournamentDoesNotExist() {
    when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());

    assertThrows(TournamentDoesNotExistException.class, () -> {
        tournamentService.getPlayersByTournament(1L);
    });

    verify(tournamentPlayerRepository, times(0)).findAllByTournament(any(Tournament.class));
    verify(restTemplate, times(0)).postForEntity(anyString(), any(), eq(PlayerDetailsDTO[].class));
}

}