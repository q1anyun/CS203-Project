package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.tournament_service.dto.DTOUtil;
import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.repository.RoundTypeRepository;
import com.chess.tms.tournament_service.repository.TournamentPlayerRepository;
import com.chess.tms.tournament_service.repository.TournamentRepository;

import jakarta.transaction.Transactional;

import com.chess.tms.tournament_service.exception.GameTypeNotFoundException;
import com.chess.tms.tournament_service.exception.MatchServiceException;
import com.chess.tms.tournament_service.exception.MaxPlayersReachedException;
import com.chess.tms.tournament_service.exception.PlayerAlreadyRegisteredException;
import com.chess.tms.tournament_service.exception.RoundTypeNotFoundException;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.exception.UserDoesNotExistException;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;

@Service
public class TournamentService {

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

    @Value("${matches.service.url}")
    private String matchServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public String createTournament(TournamentRegistrationDTO dto, long creatorId) {
        Tournament tournament = DTOUtil.convertDTOToTournament(dto, creatorId);
        tournament.setStatus(Status.UPCOMING);
        GameType gameType = gameTypeRepository.getGameTypeById(dto.getTimeControl())
                .orElseThrow(() -> new GameTypeNotFoundException("GameType does not exist."));
        tournament.setTimeControl(gameType);
        tournament.setCurrentPlayers(0);

        tournamentRepository.save(tournament);

        return "Tournament created successfully";
    }

    public String startTournament(long tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();
        ResponseEntity<Long> response;

        try {
            response = restTemplate.postForEntity(
                    matchServiceUrl + "/api/matches/" + tournamentId + "/" + tournament.getTimeControl().getId()
                            + "/generate",
                    null,
                    Long.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service error: "
                    + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service communication error", ex);
        }

        tournament.setStatus(Status.LIVE);
        Optional<RoundType> roundTypeOptional = roundTypeRepository.findById(response.getBody());
        if (roundTypeOptional.isEmpty()) {
            throw new RoundTypeNotFoundException("RoundType with id " + response.getBody() + " does not exist.");
        }

        tournament.setCurrentRound(roundTypeOptional.get());
        tournamentRepository.save(tournament);

        return tournament.getName() + " has started and current round is " + roundTypeOptional.get().getRoundName();
    }

    public TournamentDetailsDTO getTournamentDetailsById(long id) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);

        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();

        TournamentDetailsDTO returnDTO = DTOUtil.convertEntryToDTO(tournament);

        if (tournament.getWinnerId() != null) {

            ResponseEntity<PlayerDetailsDTO> response = restTemplate.getForEntity(
                    playerServiceUrl + "/api/player/" + tournament.getWinnerId(),
                    PlayerDetailsDTO.class);

            PlayerDetailsDTO winner = response.getBody();
            returnDTO.setWinner(winner);
        }

        return returnDTO;
    }

    @Transactional
    public void deleteTournament(long id) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();

        TournamentDetailsDTO returnDTO = DTOUtil.convertEntryToDTO(tournament);

        tournamentRepository.deleteById(id);
    }

    public List<TournamentDetailsDTO> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentDetailsDTO> tournamentDTOs = new ArrayList<>();

        for (Tournament tournament : tournaments) {
            tournamentDTOs.add(DTOUtil.convertEntryToDTO(tournament));
        }

        return tournamentDTOs;
    }

    public void updateTournament(long id, TournamentUpdateRequestDTO dto) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);

        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();

        if (dto.getStartDate() != null) {
            tournament.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            tournament.setEndDate(dto.getEndDate());
        }
        if (dto.getMinElo() != null) {
            tournament.setMinElo(dto.getMinElo());
        }
        if (dto.getMaxElo() != null) {
            tournament.setMaxElo(dto.getMaxElo());
        }
        if (dto.getMaxPlayers() != null) {
            tournament.setMaxPlayers(dto.getMaxPlayers());
        }
        if (dto.getTimeControl() != null) {
            GameType gameType = gameTypeRepository.getGameTypeById(dto.getTimeControl())
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType does not exist."));
            tournament.setTimeControl(gameType);
        }

        // Save the updated tournament
        tournamentRepository.save(tournament);
    }

    public void registerPlayer(long playerId, long tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();

        if (tournament.getCurrentPlayers() >= tournament.getMaxPlayers()) {
            throw new MaxPlayersReachedException("Tournament has reached the maximum number of players.");
        }

        Optional<TournamentPlayer> tp = tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(playerId, tournamentId);

        if (tp.isPresent()) {
            throw new PlayerAlreadyRegisteredException(
                    "Player with id " + playerId + " is already registered for the tournament.");
        }

        TournamentPlayer player = new TournamentPlayer();
        player.setPlayerId(playerId);
        player.setTournament(tournament);

        tournamentPlayerRepository.save(player);

        tournament.setCurrentPlayers(tournament.getCurrentPlayers() + 1);
        tournamentRepository.save(tournament);
    }

    public List<PlayerDetailsDTO> getPlayersByTournament(long tournamentId) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentId);

        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        List<Long> tournametPlayerIds = tournamentPlayerRepository.findAllByTournament(tournament.get())
                .stream()
                .map(TournamentPlayer::getPlayerId)
                .collect(Collectors.toList());

        ResponseEntity<PlayerDetailsDTO[]> response = restTemplate.postForEntity(
                playerServiceUrl + "/api/player/list",
                tournametPlayerIds,
                PlayerDetailsDTO[].class);

        PlayerDetailsDTO[] playerDetailsArray = response.getBody();

        return Arrays.asList(playerDetailsArray);

    }

    public String completeTournament(long id, long winnerId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament does not exist.");
        }
        Optional<TournamentPlayer> tp = tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(winnerId, id);

        if (tp.isEmpty()) {
            throw new UserDoesNotExistException("Winner does not exist in this tournament.");
        }

        Tournament tournament = tournamentOptional.get();
        tournament.setWinnerId(winnerId);
        tournament.setStatus(Status.COMPLETED);
        tournamentRepository.save(tournament);

        return tournament.getName() + " has been completed";
    }

    @Transactional
    public void deletePlayerFromTournament(long id, long tournamentid) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentid);
        Optional<TournamentPlayer> player = tournamentPlayerRepository.findById(id);
        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentid + " does not exist.");
        }

        if (player.isEmpty()) {
            throw new UserDoesNotExistException("Tournament Player with id " + id + " does not exist.");
        }

        tournamentPlayerRepository.deleteByPlayerId(id);
    }

    public void updateCurrentRoundForTournament(long tournamentId, long roundTypeId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentDoesNotExistException(
                        "Tournament with id " + tournamentId + " does not exist."));

        RoundType currentRound = roundTypeRepository.findById(roundTypeId)
                .orElseThrow(
                        () -> new RoundTypeNotFoundException("RoundType with id " + roundTypeId + " does not exist."));

        tournament.setCurrentRound(currentRound);
        tournamentRepository.save(tournament);
    }

    public List<TournamentDetailsDTO> getRegisteredTournaments(long playerId) {
        List<TournamentPlayer> tournamentPlayers = tournamentPlayerRepository.findAllByPlayerId(playerId);
        List<TournamentDetailsDTO> tournamentDetails = new ArrayList<>();

        for (TournamentPlayer tp : tournamentPlayers) {
            tournamentDetails.add(DTOUtil.convertEntryToDTO(tp.getTournament()));
        }

        return tournamentDetails;
    }

    public List<TournamentDetailsDTO> getLiveTournaments(long playerId) {

        List<TournamentPlayer> tournamentPlayers = tournamentPlayerRepository.findAllByPlayerId(playerId);
        List<TournamentDetailsDTO> liveTournaments = new ArrayList<>();

        for (TournamentPlayer tp : tournamentPlayers) {
            Tournament tournament = tp.getTournament();
            if (tournament.getStatus() == Status.LIVE) {
                liveTournaments.add(DTOUtil.convertEntryToDTO(tournament));
            }
        }

        return liveTournaments;
    }
}
