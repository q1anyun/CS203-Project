package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.tournament_service.dto.DTOUtil;
import com.chess.tms.tournament_service.dto.PlayerRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateDTO;
import com.chess.tms.tournament_service.enums.RegistrationStatus;
import com.chess.tms.tournament_service.enums.Status;
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
    private RestTemplate restTemplate;


    public TournamentDetailsDTO createTournament(TournamentRegistrationDTO dto, long creatorId) {
        
        // Map DTO to entry
        Tournament tournament = DTOUtil.convertDTOToTournament(dto, creatorId);
        tournament.setStatus(Status.UPCOMING);

        // Save in repo
        tournamentRepository.save(tournament);

        // Map saved entity to DTO and return
        TournamentDetailsDTO responseDTO = DTOUtil.convertEntryToDTO(tournament);

        return responseDTO;                                                             
    }

    public TournamentDetailsDTO getTournamentDetailsById(long id) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        TournamentDetailsDTO returnDTO = DTOUtil.convertEntryToDTO(tournament);
        return returnDTO;
    }

    public TournamentDetailsDTO deleteTournament(long id) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

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

    public TournamentUpdateDTO updateTournament(long id, TournamentDetailsDTO dto) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        TournamentDetailsDTO oldDTO = DTOUtil.convertEntryToDTO(tournament);

        // set new tournament fields
        tournament.setName(dto.getName());
        tournament.setCreatorId(dto.getCreatorId());
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setMinElo(dto.getMinElo());
        tournament.setMaxElo(dto.getMaxElo());
        tournament.setTotalPlayers(dto.getTotalPlayers());
        tournament.setCurrentPlayers(dto.getCurrentPlayers());;
        tournament.setStatus(dto.getStatus());
        tournament.setTimeControl(dto.getTimeControl());

        TournamentDetailsDTO updatedDTO = DTOUtil.convertEntryToDTO(tournament);

        tournamentRepository.save(tournament); 

        return new TournamentUpdateDTO(oldDTO, updatedDTO);
    }

    public PlayerRegistrationDTO registerPlayer(long playerid, long tournamentid) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentid);
        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentid + " does not exist.");
        }

        //code to check if userdoesexist. if not, throw UserDoesNotExistException

        TournamentPlayer player = new TournamentPlayer();
        player.setPlayerId(playerid);
        player.setTournament(tournament.get());
        player.setRegistrationStatus(RegistrationStatus.REGISTERED);

        tournamentPlayerRepository.save(player);

        return new PlayerRegistrationDTO(playerid, tournamentid, RegistrationStatus.REGISTERED);
    }

    public List<PlayerRegistrationDTO> getPlayersByTournament(long tournamentId) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentId);
        List<PlayerRegistrationDTO> dtoList = new ArrayList<>();

        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        for (TournamentPlayer p : tournamentPlayerRepository.findAllByTournament(tournament.get())) {
            dtoList.add(DTOUtil.convertPlayerEntrytoPlayerDTO(p));
        }

        return dtoList;
      
    }

    public List<PlayerRegistrationDTO> getAllPlayers() {
        List<PlayerRegistrationDTO> list = new ArrayList<>();

        for (TournamentPlayer p : tournamentPlayerRepository.findAll()) {
            list.add(DTOUtil.convertPlayerEntrytoPlayerDTO(p));
        }

        return list;
    }

    @Transactional
    public PlayerRegistrationDTO deletePlayerFromTournament(long playerid, long tournamentid) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentid);
        Optional<TournamentPlayer> player = tournamentPlayerRepository.findByPlayerId(playerid);
        if(tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentid + " does not exist.");
        }

        if(player.isEmpty()) {
            throw new UserDoesNotExistException("Player with id " + playerid + " does not exist.");
        }

        tournamentPlayerRepository.deleteByPlayerId(playerid);

        return new PlayerRegistrationDTO(playerid, tournamentid, RegistrationStatus.WITHDRAWN);
    }
}
