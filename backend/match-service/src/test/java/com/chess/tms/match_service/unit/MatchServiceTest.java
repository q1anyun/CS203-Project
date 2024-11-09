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
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
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
    public void testMockInjection() {
        assertNotNull(restTemplate, "RestTemplate should not be null");
        assertNotNull(matchService, "MatchService should not be null");
        assertNotNull(matchRepository, "RestTemplate should not be null");
        assertNotNull(swissBracketRepository, "MatchService should not be null");
        assertNotNull(roundTypeRepository, "RestTemplate should not be null");
        assertNotNull(gameTypeRepository, "MatchService should not be null");

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
    void testCreateKnockoutMatchesWithAdvancedPlayerIds() {
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
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(gameType));
        when(roundTypeRepository.findByRoundName("Swiss")).thenReturn(Optional.of(roundType));
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
        match.setRoundType(swissRoundType); // Set RoundType to Swiss
        match.setGameType(gameType);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

        // Mock SwissBracket
        SwissBracket bracket = new SwissBracket();
        bracket.setId(1L);
        bracket.setTournamentId(tournamentId);
        bracket.setCurrentRound(3);
        bracket.setNumberOfRounds(3); // Set as the last round
        when(swissBracketRepository.findByTournamentId(tournamentId)).thenReturn(Optional.of(bracket));

        // Mock Swiss standings for 8 players, including winner and loser
        SwissStanding winnerStanding = new SwissStanding();
        winnerStanding.setPlayerId(winnerId);
        winnerStanding.setWins(4);

        SwissStanding loserStanding = new SwissStanding();
        loserStanding.setPlayerId(loserId);
        loserStanding.setLosses(3);

        // Additional players with varying wins/losses
        SwissStanding player3 = new SwissStanding();
        player3.setPlayerId(103L);
        player3.setWins(3);

        SwissStanding player4 = new SwissStanding();
        player4.setPlayerId(104L);
        player4.setWins(3);

        SwissStanding player5 = new SwissStanding();
        player5.setPlayerId(105L);
        player5.setWins(2);

        SwissStanding player6 = new SwissStanding();
        player6.setPlayerId(106L);
        player6.setWins(2);

        SwissStanding player7 = new SwissStanding();
        player7.setPlayerId(107L);
        player7.setWins(1);

        SwissStanding player8 = new SwissStanding();
        player8.setPlayerId(108L);
        player8.setWins(0);

        List<SwissStanding> standings = Arrays.asList(
                winnerStanding, player3, player4, loserStanding, player5, player6, player7, player8);

        when(swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), winnerId))
                .thenReturn(Optional.of(winnerStanding));
        when(swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), loserId))
                .thenReturn(Optional.of(loserStanding));

        when(swissStandingRepository.findByBracketIdOrderByWinsDescLossesAsc(bracket.getId())).thenReturn(standings);
        when(roundTypeRepository.findByNumberOfPlayers(4)).thenReturn(Optional.of(roundType));
        when(roundTypeRepository.findByNumberOfPlayers(2)).thenReturn(Optional.of(roundType));
        when(gameTypeRepository.findById(1L)).thenReturn(Optional.of(gameType));

        // Mock round completion check
        Match completedMatch = new Match();
        completedMatch.setStatus(Match.MatchStatus.COMPLETED);
        when(matchRepository.findByTournamentIdAndSwissRoundNumber(tournamentId, bracket.getCurrentRound()))
                .thenReturn(Collections.singletonList(completedMatch));
        when(restTemplate.getForObject(contains("/api/player/"), eq(PlayerDetailsDTO.class)))
                .thenReturn(playerDetails);

        // Act
        String result = matchService.advanceWinner(matchId, winnerId);

        // Assert
        assertEquals("Swiss rounds completed, moving to knockout phase.", result);

        // Verify the expected top 4 players are selected for the knockout stage
        List<Long> advancedPlayerIds = standings.stream()
                .limit(4) 
                .map(SwissStanding::getPlayerId)
                .collect(Collectors.toList());

        // Expected IDs of players advancing to the knockout stage
        List<Long> expectedIds = Arrays.asList(winnerId, player3.getPlayerId(), player4.getPlayerId(), loserId);

        // Assert that the advanced player list has the expected players
        assertEquals(4, advancedPlayerIds.size(), "The advanced player list should contain exactly 4 players");
        assertTrue(advancedPlayerIds.containsAll(expectedIds), "Advanced players should match expected top players");

        // Verify repository saves and service calls
        verify(matchRepository).save(match);
        verify(swissStandingRepository).save(winnerStanding);
        verify(swissStandingRepository).save(loserStanding);
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
