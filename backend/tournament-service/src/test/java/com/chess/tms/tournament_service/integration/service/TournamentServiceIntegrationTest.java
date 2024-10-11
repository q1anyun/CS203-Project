package com.chess.tms.tournament_service.integration.service;

import com.chess.tms.tournament_service.dto.*;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import com.chess.tms.tournament_service.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import org.springframework.web.client.RestTemplate;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TournamentServiceIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private TournamentRepository tournamentRepository;

        @Autowired
        private TournamentPlayerRepository tournamentPlayerRepository;

        @Autowired
        private GameTypeRepository gameTypeRepository;

        @Autowired
        private RoundTypeRepository roundTypeRepository;

        @Autowired
        private RestTemplate restTemplate;

        private MockRestServiceServer mockServer;

        @Value("${matches.service.url}")
        private String matchServiceUrl;

        @Value("${players.service.url}")
        private String playerServiceUrl;

        @BeforeEach
        public void setup() {
                mockServer = MockRestServiceServer.createServer(restTemplate);
                tournamentRepository.deleteAll();
                tournamentPlayerRepository.deleteAll();

                if (gameTypeRepository.findById(1L).isEmpty()) {
                        GameType gameType = new GameType();
                        gameType.setId(1L);
                        gameType.setName("Blitz");
                        gameType.setTimeControlMinutes(5);
                        gameTypeRepository.save(gameType);
                }

                if (roundTypeRepository.findById(1L).isEmpty()) {
                        RoundType roundType = new RoundType();
                        roundType.setId(1L);
                        roundType.setRoundName("Top 16");
                        roundType.setNumberOfPlayers(16);
                        roundTypeRepository.save(roundType);
                }
        }

        @Test
        public void testCreateTournament() throws Exception {

                TournamentRegistrationDTO dto = new TournamentRegistrationDTO();
                dto.setName("Test Tournament");
                dto.setStartDate(LocalDateTime.now().plusDays(1));
                dto.setEndDate(LocalDateTime.now().plusDays(2));
                dto.setTimeControl(1);
                dto.setMaxPlayers(32);
                dto.setMinElo(1000);
                dto.setMaxElo(2000);

                mockMvc.perform(post("/api/tournaments")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .header("X-User-Id", "1"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Tournament created successfully")));

                assertEquals(1, tournamentRepository.findAll().size());
        }

        @Test
        public void testUpdateTournament() throws Exception {
                Tournament tournament = createTournament();

                TournamentUpdateRequestDTO updateDTO = new TournamentUpdateRequestDTO();
                updateDTO.setMaxPlayers(50);
                updateDTO.setMinElo(1200);

                mockMvc.perform(put("/api/tournaments/" + tournament.getTournamentId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Tournament updated successfully"));

                Tournament updatedTournament = tournamentRepository.findById(tournament.getTournamentId())
                                .orElseThrow();
                assertEquals(50, updatedTournament.getMaxPlayers());
                assertEquals(1200, updatedTournament.getMinElo());
        }

        @Test
        public void testDeleteTournament() throws Exception {
                Tournament tournament = createTournament();

                mockMvc.perform(delete("/api/tournaments/" + tournament.getTournamentId()))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Sucessfully deleted tournament"));

                assertFalse(tournamentRepository.findById(tournament.getTournamentId()).isPresent());
        }

        @Test
        public void testStartTournament() throws Exception {
                Tournament tournament = createTournament();

                mockServer.expect(requestTo(
                                matchServiceUrl + "/api/matches/" + tournament.getTournamentId() + "/1/generate"))
                                .andRespond(withStatus(HttpStatus.OK)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body("1"));

                mockMvc.perform(post("/api/tournaments/start/" + tournament.getTournamentId()))
                                .andExpect(status().isOk())
                                .andExpect(content()
                                                .string(containsString("has started and current round is Top 16")));

                Tournament startedTournament = tournamentRepository.findById(tournament.getTournamentId())
                                .orElseThrow();
                assertEquals(Status.LIVE, startedTournament.getStatus());
        }

        @Test
        public void testCompleteTournament() throws Exception {
                Tournament tournament = createTournament();
                registerPlayerForTournament(tournament, 100L);

                mockMvc.perform(put("/api/tournaments/" + tournament.getTournamentId() + "/winner/100"))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("has been completed")));

                Tournament completedTournament = tournamentRepository.findById(tournament.getTournamentId())
                                .orElseThrow();
                assertEquals(Status.COMPLETED, completedTournament.getStatus());
        }

        private Tournament createTournament() {
                Tournament tournament = new Tournament();
                tournament.setCreatorId(1L);
                tournament.setName("New Tournament");
                tournament.setMaxPlayers(32);
                tournament.setCurrentPlayers(0);
                tournament.setTimeControl(gameTypeRepository.findById(1L).orElseThrow());
                tournament.setStatus(Status.UPCOMING);
                tournament.setStartDate(LocalDateTime.now().plusDays(1));
                tournament.setEndDate(LocalDateTime.now().plusDays(2));
                return tournamentRepository.save(tournament);
        }

        private void registerPlayerForTournament(Tournament tournament, long playerId) {
                TournamentPlayer player = new TournamentPlayer();
                player.setPlayerId(playerId);
                player.setTournament(tournament);
                tournamentPlayerRepository.save(player);
                tournament.setCurrentPlayers(tournament.getCurrentPlayers() + 1);
                tournamentRepository.save(tournament);
        }
}