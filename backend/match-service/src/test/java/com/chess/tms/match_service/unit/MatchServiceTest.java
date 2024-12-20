package com.chess.tms.match_service.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.match_service.dto.*;
import com.chess.tms.match_service.exception.*;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.*;
import com.chess.tms.match_service.service.MatchService;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    private MatchesRepository matchRepository;

    @Mock
    private RoundTypeRepository roundTypeRepository;

    @Mock
    private GameTypeRepository gameTypeRepository;

    @Mock
    private SwissBracketRepository swissBracketRepository;

    @Mock
    private SwissStandingRepository swissStandingRepository;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private MatchService matchService;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    private Match match;
    private GameType gameType;
    private RoundType roundType;
    private TournamentPlayerEloDTO[] players;
    private PlayerDetailsDTO playerDetails;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Setup basic GameType
        gameType = new GameType();
        gameType.setId(1L);
        gameType.setName("Rapid");

        // Setup basic RoundType
        roundType = new RoundType();
        roundType.setId(1L);
        roundType.setRoundName("Quarterfinal");

        // Setup basic Match
        match = new Match();
        match.setId(1L);
        match.setTournamentId(1L);
        match.setPlayer1Id(1L);
        match.setPlayer2Id(2L);
        match.setGameType(gameType);
        match.setRoundType(roundType);
        match.setStatus(Match.MatchStatus.PENDING);

        // Setup player details
        playerDetails = new PlayerDetailsDTO();
        playerDetails.setId(1L);
        playerDetails.setFirstName("John");
        playerDetails.setLastName("Doe");
        playerDetails.setEloRating(1500);

        // Setup tournament players
        players = new TournamentPlayerEloDTO[] {
                new TournamentPlayerEloDTO(1L, 1600),
                new TournamentPlayerEloDTO(2L, 1500),
                new TournamentPlayerEloDTO(3L, 1400),
                new TournamentPlayerEloDTO(4L, 1300)
        };
    }

    

    @Test
    void createKnockoutMatches_ValidInput_Success() {
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(gameType));
        when(roundTypeRepository.findByNumberOfPlayers(4)).thenReturn(Optional.of(roundType));
        when(roundTypeRepository.findByNumberOfPlayers(2)).thenReturn(Optional.of(roundType));

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(TournamentPlayerEloDTO[].class))).thenReturn(new ResponseEntity<>(players, HttpStatus.OK));
        when(matchRepository.saveAll(any())).thenReturn(Arrays.asList(match));

        Long result = matchService.createKnockoutMatches(1L, 1L, null);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(matchRepository, times(1)).saveAll(any());
    }

    @Test
    void createKnockoutMatchesWithAdvancedPlayerIds() {
        Long tournamentId = 1L;
        Long gameTypeId = 2L;

        List<Long> advancedPlayerIds = Arrays.asList(101L, 102L, 103L, 104L, 105L);

        for (Long playerId : advancedPlayerIds) {
            when(restTemplate.getForObject(playerServiceUrl + "/api/player/" + playerId, PlayerDetailsDTO.class))
                    .thenReturn(playerDetails);
        }

        when(gameTypeRepository.findById(2L)).thenReturn(Optional.of(gameType));
        when(roundTypeRepository.findByNumberOfPlayers(8)).thenReturn(Optional.of(roundType));
        when(roundTypeRepository.findByNumberOfPlayers(4)).thenReturn(Optional.of(roundType));
        when(roundTypeRepository.findByNumberOfPlayers(2)).thenReturn(Optional.of(roundType));

        when(matchRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Match> matches = invocation.getArgument(0);
            matches.forEach(match -> match.setId(new Random().nextLong())); // Simulate setting ID on save
            return matches;
        });

        Long roundId = matchService.createKnockoutMatches(tournamentId, gameTypeId, advancedPlayerIds);
        verify(matchRepository, times(1)).saveAll(anyList());
        assertNotNull(roundId, "Round ID should not be null");

    }

    @Test
    void createSwissMatches_ValidInput_Success() {
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                eq(null),
                eq(TournamentPlayerEloDTO[].class))).thenReturn(new ResponseEntity<>(players, HttpStatus.OK));
        when(swissBracketRepository.save(any(SwissBracket.class))).thenAnswer(i -> {
            SwissBracket bracket = i.getArgument(0);
            bracket.setId(1L);
            return bracket;
        });

        Long result = matchService.createSwissMatches(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result);
        verify(swissBracketRepository, times(1)).save(any());
        verify(swissStandingRepository, atLeast(1)).save(any());
    }

    @Test
    void getMatchesByTournament_ValidInput_Success() {
        when(matchRepository.findByTournamentId(1L)).thenReturn(Arrays.asList(match));
        when(restTemplate.getForObject(anyString(), eq(TournamentDTO.class)))
                .thenReturn(new TournamentDTO());
        when(restTemplate.getForObject(contains("/api/player/"), eq(PlayerDetailsDTO.class)))
                .thenReturn(playerDetails);

        List<MatchDTO> result = matchService.getMatchesByTournament(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void getMatch_ValidId_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(restTemplate.getForObject(contains("/api/player/"), eq(PlayerDetailsDTO.class)))
                .thenReturn(playerDetails);

        when(restTemplate.getForObject(anyString(), eq(TournamentDTO.class)))
                .thenReturn(new TournamentDTO());

        MatchDTO result = matchService.getMatch(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getMatch_InvalidId_ThrowsException() {
        when(matchRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(MatchDoesNotExistException.class, () -> {
            matchService.getMatch(1L);
        });
    }

    @Test
    void advanceWinner_ValidKnockoutMatch_Success() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));
        when(matchRepository.save(any(Match.class))).thenReturn(match);

        String result = matchService.advanceWinner(1L, 1L);

        assertNotNull(result);
        assertTrue(result.contains("Winner advanced") || result.contains("Tournament completed"));
        verify(matchRepository, times(1)).save(any(Match.class));
    }

    @Test
    void advanceWinner_ValidSwissMatch_Success() {
        // Arrange
        Long matchId = 1L;
        Long winnerId = 101L;
        Long loserId = 102L;
        Long tournamentId = 1L;

        // Mock RoundType for Swiss
        RoundType swissRoundType = new RoundType();
        swissRoundType.setId(1L);
        swissRoundType.setRoundName("Swiss");

        // Mock Match with Swiss RoundType
        Match match = new Match();
        match.setId(matchId);
        match.setTournamentId(tournamentId);
        match.setPlayer1Id(winnerId);
        match.setPlayer2Id(loserId);
        match.setRoundType(swissRoundType);
        match.setGameType(gameType);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        // Mock SwissBracket
        SwissBracket bracket = new SwissBracket();
        bracket.setId(1L);
        bracket.setTournamentId(tournamentId);
        bracket.setCurrentRound(3);
        bracket.setNumberOfRounds(3);
        when(swissBracketRepository.findByTournamentId(tournamentId)).thenReturn(Optional.of(bracket));

        // Initialize standings with proper wins/losses values
        SwissStanding winnerStanding = new SwissStanding();
        winnerStanding.setPlayerId(winnerId);
        winnerStanding.setWins(4);  // Initialize wins
        winnerStanding.setLosses(0);  // Initialize losses
        winnerStanding.setBracket(bracket);

        SwissStanding loserStanding = new SwissStanding();
        loserStanding.setPlayerId(loserId);
        loserStanding.setWins(3);  // Initialize wins
        loserStanding.setLosses(1);  // Initialize losses
        loserStanding.setBracket(bracket);

        // Additional players with properly initialized standings
        List<SwissStanding> standings = Arrays.asList(
            winnerStanding,
            createStanding(103L, 3, 1, bracket),
            createStanding(104L, 3, 1, bracket),
            createStanding(105L, 2, 2, bracket),
            createStanding(106L, 2, 2, bracket),
            createStanding(107L, 1, 3, bracket),
            createStanding(108L, 0, 4, bracket),
            loserStanding
        );

        when(swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), winnerId))
                .thenReturn(Optional.of(winnerStanding));
        when(swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), loserId))
                .thenReturn(Optional.of(loserStanding));
        when(swissStandingRepository.findByBracketIdOrderByWinsDescLossesAsc(bracket.getId()))
                .thenReturn(standings);

        when(roundTypeRepository.findByNumberOfPlayers(4)).thenReturn(Optional.of(roundType));
        when(roundTypeRepository.findByNumberOfPlayers(2)).thenReturn(Optional.of(roundType));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(gameType));

        when(matchRepository.findByTournamentIdAndSwissRoundNumber(tournamentId, bracket.getCurrentRound()))
                .thenReturn(Collections.singletonList(match));
        when(restTemplate.getForObject(contains("/api/player/"), eq(PlayerDetailsDTO.class)))
                .thenReturn(playerDetails);

        // Act
        String result = matchService.advanceWinner(matchId, winnerId);

        // Assert
        assertEquals("Swiss rounds completed, moving to knockout phase.", result);
        verify(matchRepository).save(match);
        verify(swissStandingRepository).save(winnerStanding);
        verify(swissStandingRepository).save(loserStanding);
    }

    // Helper method to create standings
    private SwissStanding createStanding(Long playerId, int wins, int losses, SwissBracket bracket) {
        SwissStanding standing = new SwissStanding();
        standing.setPlayerId(playerId);
        standing.setWins(wins);
        standing.setLosses(losses);
        standing.setBracket(bracket);
        return standing;
    }

    @Test
    void advanceWinner_InvalidPlayer_ThrowsException() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(PlayerDoesNotExistInMatchException.class, () -> {
            matchService.advanceWinner(1L, 3L);
        });
    }

    @Test
    void advanceWinner_AlreadyCompleted_ThrowsException() {
        match.setStatus(Match.MatchStatus.COMPLETED);
        match.setWinnerId(1L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(match));

        assertThrows(MatchAlreadyCompletedException.class, () -> {
            matchService.advanceWinner(1L, 1L);
        });
    }

    @Test
    void getRecentMatchesByPlayerId_ValidInput_Success() {
        when(matchRepository.findTop5ByPlayer1IdOrPlayer2IdOrderByUpdatedAtDesc(1L, 1L))
                .thenReturn(Arrays.asList(match));
        when(restTemplate.getForObject(anyString(), eq(TournamentDTO.class)))
                .thenReturn(new TournamentDTO());
        when(restTemplate.getForObject(contains("/api/player/"), eq(PlayerDetailsDTO.class)))
                .thenReturn(playerDetails);

        List<MatchDTO> result = matchService.getRecentMatchesByPlayerId(1L);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
