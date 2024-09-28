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
import org.springframework.web.client.RestTemplate;

import com.chess.tms.tournament_service.dto.DTOUtil;
import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.repository.TournamentPlayerRepository;
import com.chess.tms.tournament_service.repository.TournamentRepository;

import jakarta.transaction.Transactional;

import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.exception.UserDoesNotExistException;
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
    private RestTemplate restTemplate;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public TournamentDetailsDTO createTournament(TournamentRegistrationDTO dto, long creatorId) {

        Tournament tournament = DTOUtil.convertDTOToTournament(dto, creatorId);
        tournament.setStatus(Status.UPCOMING);
        tournament.setTimeControl(gameTypeRepository.getGameTypeById(dto.getTimeControl()));

        tournamentRepository.save(tournament);

        TournamentDetailsDTO responseDTO = DTOUtil.convertEntryToDTO(tournament);

        return responseDTO;
    }

    public TournamentDetailsDTO getTournamentDetailsById(long id) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
    
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }
    
        Tournament tournament = tournamentOptional.get();
    

        return DTOUtil.convertEntryToDTO(tournament);
    }

    public TournamentDetailsDTO deleteTournament(long id) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }
    
        Tournament tournament = tournamentOptional.get();

        TournamentDetailsDTO returnDTO = DTOUtil.convertEntryToDTO(tournament);

        tournamentRepository.deleteById(id);
    
        return returnDTO;
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
        if (dto.getCurrentPlayers() != null) {
            tournament.setCurrentPlayers(dto.getCurrentPlayers());
        }
        if (dto.getTimeControl() != null) {
            tournament.setTimeControl(gameTypeRepository.getGameTypeById(dto.getTimeControl()));
        }
    
        // Save the updated tournament
        tournamentRepository.save(tournament);
    }

    public void registerPlayer(long playerid, long tournamentid) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentid);
        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentid + " does not exist.");
        }

        TournamentPlayer player = new TournamentPlayer();
        player.setPlayerId(playerid);
        player.setTournament(tournament.get());

        tournamentPlayerRepository.save(player);
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

    // public List<PlayerRegistrationDTO> getAllPlayers() {
    //     List<PlayerRegistrationDTO> list = new ArrayList<>();

    //     for (TournamentPlayer p : tournamentPlayerRepository.findAll()) {
    //         list.add(DTOUtil.convertPlayerEntrytoPlayerDTO(p));
    //     }

    //     return list;
    // }

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
}
