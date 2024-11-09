package com.chess.tms.match_service.integration;

import com.chess.tms.match_service.dto.MatchDTO;
import com.chess.tms.match_service.model.GameType;
import com.chess.tms.match_service.model.Match;
import com.chess.tms.match_service.model.Match.MatchStatus;
import com.chess.tms.match_service.model.RoundType;
import com.chess.tms.match_service.model.SwissBracket;
import com.chess.tms.match_service.model.SwissStanding;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;
import com.chess.tms.match_service.repository.SwissBracketRepository;
import com.chess.tms.match_service.repository.SwissStandingRepository;
import com.chess.tms.match_service.service.MatchService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MatchServiceIntegrationTest {

        @Autowired
        private MatchService matchService;

        @Autowired
        private MatchesRepository matchRepository;

        @Autowired
        private RoundTypeRepository roundTypeRepository;

        @Autowired
        private GameTypeRepository gameTypeRepository;

        @Autowired
        private SwissBracketRepository swissBracketRepository;

        @Autowired
        private SwissStandingRepository swissStandingRepository;

        @Autowired
        private RestTemplate externalRestTemplate;

        private MockRestServiceServer mockServer;

        @Value("${tournaments.service.url}")
        private String tournamentServiceUrl;

        @Value("${elo.service.url}")
        private String eloServiceUrl;

        @Value("${players.service.url}")
        private String playerServiceUrl;

        // Store common test data
        private RoundType semiFinals;
        private RoundType finals;
        private GameType standardChess;

        @BeforeEach
        public void setup() {
                // Reset mock server before each test
                mockServer = MockRestServiceServer.bindTo(externalRestTemplate).build();

                matchRepository.deleteAll();
                swissBracketRepository.deleteAll();
                swissStandingRepository.deleteAll();
                roundTypeRepository.deleteAll();
             

                // Insert initial data
                semiFinals = new RoundType();
                semiFinals.setRoundName("Semi Finals");
                semiFinals.setNumberOfPlayers(4);
                semiFinals = roundTypeRepository.save(semiFinals);

                finals = new RoundType();
                finals.setRoundName("Finals");
                finals.setNumberOfPlayers(2);
                finals = roundTypeRepository.save(finals);

                standardChess = new GameType();
                standardChess.setName("Standard Chess");
                standardChess.setTimeControlMinutes(5);
                standardChess = gameTypeRepository.save(standardChess);
        }

        @Test
        public void createKnockoutMatches_ShouldReturnRoundTypeId() {
                matchRepository.deleteAll();
                //Act
                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournament-players/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("[{\"id\":1,\"eloRating\":2000},{\"id\":2,\"eloRating\":1800},{\"id\":3,\"eloRating\":1900},{\"id\":4,\"eloRating\":1700}]")
                                                .contentType(MediaType.APPLICATION_JSON));
                //Arrange
                Long roundTypeId = matchService.createKnockoutMatches(1L, 1L, null);

                
                //Assert
                assertNotNull(roundTypeId);
                List<Match> matches = matchRepository.findAll();
                assertFalse(matches.isEmpty());
                assertEquals(3, matches.size());

                Match firstMatch = matches.get(0);
                assertNotNull(firstMatch.getRoundType());
                assertEquals("Semi Finals", firstMatch.getRoundType().getRoundName());

                mockServer.verify();
        }

        @Test
        public void createKnockoutMatches_withAdvancedPlayerIds() {
                matchRepository.deleteAll();
                //Arrange
                List<Long> advancedPlayerIds = Arrays.asList(1L, 2L);
                mockServer.expect(requestTo(playerServiceUrl + "/api/player/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":1,\"userId\":101,\"eloRating\":2000,\"firstName\":\"John\",\"lastName\":\"Doe\",\"profilePicture\":\"http://example.com/profile.jpg\",\"totalWins\":15,\"totalLosses\":5,\"totalMatches\":20,\"country\":\"USA\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(playerServiceUrl + "/api/player/2"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":2,\"userId\":102,\"eloRating\":1800,\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"profilePicture\":\"http://example.com/profile2.jpg\",\"totalWins\":20,\"totalLosses\":10,\"totalMatches\":30,\"country\":\"Canada\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                Long roundTypeId = matchService.createKnockoutMatches(1L, 1L, advancedPlayerIds);

                assertNotNull(roundTypeId);

                List<Match> matches = matchRepository.findAll();
                assertFalse(matches.isEmpty());
                assertEquals(1, matches.size());

                Match firstMatch = matches.get(0);
                assertNotNull(firstMatch.getRoundType());
                assertEquals("Finals", firstMatch.getRoundType().getRoundName());
                mockServer.verify();
        }

        @Test
        public void createSwissMatches_shouldCreateSwissRounds() {
                swissBracketRepository.deleteAll();
                // Arrange
                Long tournamentId = 1L;
                Long gameTypeId = 1L;

                RoundType swiss = new RoundType();
                swiss.setRoundName("Swiss");
                swiss.setNumberOfPlayers(4);
                swiss = roundTypeRepository.save(swiss);

                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournament-players/" + tournamentId))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("[{\"id\":1,\"eloRating\":2000},{\"id\":2,\"eloRating\":1800},{\"id\":3,\"eloRating\":1900},{\"id\":4,\"eloRating\":1700}]")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Act
                Long swissBracketId = matchService.createSwissMatches(tournamentId, gameTypeId);

                // Assert
                assertEquals(2L, swissBracketId);
                SwissBracket bracket = swissBracketRepository.findById(swissBracketId).orElseThrow();
                assertEquals(1, bracket.getCurrentRound());
                assertEquals(3, bracket.getNumberOfRounds());
        }

        @Test
        public void advanceWinner_SwissMatch() {
                matchRepository.deleteAll();
                swissBracketRepository.deleteAll();
                //Arrange 
                SwissBracket bracket = new SwissBracket();
                bracket.setTournamentId(1L);
                bracket.setCurrentRound(1);
                bracket.setNumberOfRounds(3);
                swissBracketRepository.save(bracket);
        
                SwissStanding player1Standing = new SwissStanding();
                player1Standing.setBracket(bracket);
                player1Standing.setTournamentPlayerId(101L);
                player1Standing.setPlayerId(1L);
                player1Standing.setWins(2);
                player1Standing.setLosses(1);
                swissStandingRepository.save(player1Standing);

                SwissStanding player2Standing = new SwissStanding();
                player2Standing.setBracket(bracket);
                player2Standing.setTournamentPlayerId(102L);
                player2Standing.setPlayerId(2L);
                player2Standing.setWins(1);
                player2Standing.setLosses(2);
                swissStandingRepository.save(player2Standing);

                swissStandingRepository.saveAll(List.of(player1Standing, player2Standing));

                RoundType swissRoundType = new RoundType();
                swissRoundType.setRoundName("Swiss");
                swissRoundType.setNumberOfPlayers(4);
                swissRoundType = roundTypeRepository.save(swissRoundType);

                Match match = new Match();
                match.setTournamentId(1L);
                match.setPlayer1Id(1L);
                match.setPlayer2Id(2L);
                match.setRoundType(swissRoundType);
                match.setGameType(standardChess);
                match.setSwissRoundNumber(1);
                match.setStatus(MatchStatus.PENDING);
                match.setCreatedAt(LocalDateTime.now());
                matchRepository.save(match);

               
                Long matchId = matchRepository.findAll().get(0).getId();
                Long winnerId = 1L;

                mockServer.expect(requestTo(eloServiceUrl + "/api/elo/match"))
                                .andRespond(withStatus(HttpStatus.OK));

                
                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournament-players/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                .body("[{\"id\":1,\"eloRating\":2000},{\"id\":2,\"eloRating\":1800},{\"id\":3,\"eloRating\":1900},{\"id\":4,\"eloRating\":1700}]")
                                .contentType(MediaType.APPLICATION_JSON));


                // Act
                String result = matchService.advanceWinner(matchId, winnerId);

                // Assert: Check match updates
                Match updatedMatch = matchRepository.findById(matchId).orElseThrow();
                assertEquals(winnerId, updatedMatch.getWinnerId());
                assertEquals(2L, updatedMatch.getLoserId());
                assertEquals(MatchStatus.COMPLETED, updatedMatch.getStatus());

                assertTrue(result.contains("Swiss standings updated") || result.contains("Advanced to Swiss Round"));
                mockServer.verify();
        }

        @Test
        public void advanceWinner_shouldUpdateMatchAndElo_TournamentCompleted() {
                matchRepository.deleteAll();

                // Arrange
                Long tournamentId = 1L;
                Long gameTypeId = 1L;
                List<Long> advancedPlayerIds = null;
                Long winnerId = 1L;

                Match match1 = new Match();
                match1.setTournamentId(1L);
                match1.setPlayer1Id(1L);
                match1.setPlayer2Id(2L);
                match1.setRoundType(finals);
                match1.setGameType(standardChess);
                match1.setStatus(MatchStatus.PENDING);
                match1.setCreatedAt(LocalDateTime.now());

                match1 = matchRepository.save(match1);
                Long savedMatchId = match1.getId();

                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournament-players/" + tournamentId))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("[{\"id\":1,\"eloRating\":2000},{\"id\":2,\"eloRating\":1800}]")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(eloServiceUrl + "/api/elo/match"))
                                .andRespond(withStatus(HttpStatus.OK));

                mockServer.expect(requestTo(
                                tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/winner/" + winnerId))
                                .andRespond(withStatus(HttpStatus.OK));

                // Act
                matchService.createKnockoutMatches(tournamentId, gameTypeId, advancedPlayerIds);
                String result = matchService.advanceWinner(savedMatchId, winnerId);

                // Assert
                assertEquals("Tournament completed", result);
                Match match = matchRepository.findById(savedMatchId).orElseThrow();
                assertEquals(winnerId, match.getWinnerId());
                assertNotNull(match.getUpdatedAt());
        }

        @Test
        public void advanceWinner_shouldUpdateMatchAndElo_TournamnentAdvanced() {
                // Clear existing matches
                matchRepository.deleteAll();

                // Arrange
                Long tournamentId = 1L;
                Long winnerId = 1L;

                Match match1 = new Match();
                match1.setTournamentId(1L);
                match1.setPlayer1Id(null);
                match1.setPlayer2Id(null);
                match1.setRoundType(finals);
                match1.setGameType(standardChess);
                match1.setStatus(MatchStatus.PENDING);
                match1.setCreatedAt(LocalDateTime.now());

                match1 = matchRepository.save(match1);
                Long savedMatchId = match1.getId();

                Match match2 = new Match();
                match2.setTournamentId(1L);
                match2.setPlayer1Id(1L);
                match2.setPlayer2Id(2L);
                match2.setRoundType(semiFinals);
                match2.setGameType(standardChess);
                match2.setStatus(MatchStatus.PENDING);
                match2.setCreatedAt(LocalDateTime.now());
                match2.setNextMatchId(savedMatchId);

                match2 = matchRepository.save(match2);
                Long savedMatchId2 = match2.getId();

                mockServer.expect(requestTo(eloServiceUrl + "/api/elo/match"))
                                .andRespond(withStatus(HttpStatus.OK));
                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/round/"
                                + finals.getId()))
                                .andRespond(withStatus(HttpStatus.OK));

                // Act
                String result = matchService.advanceWinner(savedMatchId2, winnerId);

                // Assert
                assertEquals("Tournament has advanced to the next round", result);
                Match match = matchRepository.findById(savedMatchId2).orElseThrow();
                assertEquals(winnerId, match.getWinnerId());
                assertNotNull(match.getUpdatedAt());
                mockServer.verify();
        }

        @Test
        public void advanceWinner_ShouldAdvanceToNextRound() {
                matchRepository.deleteAll();

                // Setup tournament with two semi-final matches and one final
                Match finalMatch = new Match();
                finalMatch.setTournamentId(1L);
                finalMatch.setRoundType(finals);
                finalMatch.setGameType(standardChess);
                finalMatch.setStatus(MatchStatus.PENDING);
                finalMatch = matchRepository.save(finalMatch);

                Match semiFinal1 = new Match();
                semiFinal1.setTournamentId(1L);
                semiFinal1.setPlayer1Id(1L);
                semiFinal1.setPlayer2Id(2L);
                semiFinal1.setRoundType(semiFinals);
                semiFinal1.setGameType(standardChess);
                semiFinal1.setStatus(MatchStatus.PENDING);
                semiFinal1.setNextMatchId(finals.getId());
                semiFinal1 = matchRepository.save(semiFinal1);

                Match semiFinal2 = new Match();
                semiFinal2.setTournamentId(1L);
                semiFinal2.setPlayer1Id(3L);
                semiFinal2.setPlayer2Id(4L);
                semiFinal2.setRoundType(semiFinals);
                semiFinal2.setGameType(standardChess);
                semiFinal2.setStatus(MatchStatus.PENDING);
                semiFinal2.setNextMatchId(finals.getId());
                semiFinal2 = matchRepository.save(semiFinal2);

                mockServer.expect(requestTo(eloServiceUrl + "/api/elo/match"))
                                .andRespond(withStatus(HttpStatus.OK));

                // Advance winner of first semi-final
                String result = matchService.advanceWinner(semiFinal1.getId(), 1L);
                assertEquals("Winner advanced to the next round", result);

                // Verify final match does not start until all the rounds are completed
                Match updatedFinals = matchRepository.findById(finalMatch.getId()).orElseThrow();
                assertNull(updatedFinals.getPlayer1Id());
                assertNull(updatedFinals.getPlayer2Id());

                mockServer.verify();
        }

        @Test
        public void getMatchesByTournament() {
                matchRepository.deleteAll();

                Long tournamentId = 1L;
                RoundType finals = new RoundType();
                finals.setRoundName("Finals");
                finals.setNumberOfPlayers(2);
                roundTypeRepository.save(finals);

                GameType standardChess = new GameType();
                standardChess.setName("Standard Chess");
                standardChess.setTimeControlMinutes(5);
                gameTypeRepository.save(standardChess);

                Match match1 = new Match();
                match1.setTournamentId(tournamentId);
                match1.setPlayer1Id(1L);
                match1.setPlayer2Id(2L);
                match1.setRoundType(finals);
                match1.setGameType(standardChess);
                match1.setStatus(MatchStatus.PENDING);
                match1.setCreatedAt(LocalDateTime.now());
                matchRepository.save(match1);

                mockServer.expect(requestTo(playerServiceUrl + "/api/player/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":1,\"userId\":101,\"eloRating\":2000,\"firstName\":\"John\",\"lastName\":\"Doe\",\"profilePicture\":\"http://example.com/profile.jpg\",\"totalWins\":15,\"totalLosses\":5,\"totalMatches\":20,\"country\":\"USA\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(playerServiceUrl + "/api/player/2"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":2,\"userId\":102,\"eloRating\":1800,\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"profilePicture\":\"http://example.com/profile2.jpg\",\"totalWins\":20,\"totalLosses\":10,\"totalMatches\":30,\"country\":\"Canada\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournaments/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":1,\"name\":\"rapid chess competition\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                List<MatchDTO> matchDTOs = matchService.getMatchesByTournament(tournamentId);

                // Verify that the returned list contains the correct number of matches
                assertEquals(1, matchDTOs.size());

                // Verify details of the first match DTO
                MatchDTO matchDTO1 = matchDTOs.get(0);
                assertEquals(1L, matchDTO1.getPlayer1().getId());
                assertEquals(2L, matchDTO1.getPlayer2().getId());
                mockServer.verify();

        }

        @Test
        public void getMatch_ReturnMatch() {

                matchRepository.deleteAll();


                Match match1 = new Match();
                match1.setTournamentId(1L);
                match1.setPlayer1Id(1L);
                match1.setPlayer2Id(2L);
                match1.setRoundType(finals);
                match1.setGameType(standardChess);
                match1.setStatus(MatchStatus.PENDING);
                match1.setCreatedAt(LocalDateTime.now());

                match1 = matchRepository.save(match1);
                Long savedMatchId = match1.getId();

                // Setup mock responses
                mockServer.expect(requestTo(playerServiceUrl + "/api/player/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":1,\"userId\":101,\"eloRating\":2000,\"firstName\":\"John\",\"lastName\":\"Doe\",\"profilePicture\":\"http://example.com/profile.jpg\",\"totalWins\":15,\"totalLosses\":5,\"totalMatches\":20,\"country\":\"USA\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(playerServiceUrl + "/api/player/2"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":2,\"userId\":102,\"eloRating\":1800,\"firstName\":\"Jane\",\"lastName\":\"Smith\",\"profilePicture\":\"http://example.com/profile2.jpg\",\"totalWins\":20,\"totalLosses\":10,\"totalMatches\":30,\"country\":\"Canada\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

                mockServer.expect(requestTo(tournamentServiceUrl + "/api/tournaments/1"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .body("{\"id\":1,\"name\":\"rapid chess competition\"}")
                                                .contentType(MediaType.APPLICATION_JSON));

             
                MatchDTO matchDTO = matchService.getMatch(savedMatchId);
                mockServer.verify();

                // Assertions
                assertNotNull(matchDTO);
                assertEquals(1L, matchDTO.getPlayer1().getId());
                assertEquals(2L, matchDTO.getPlayer2().getId());
        }

}