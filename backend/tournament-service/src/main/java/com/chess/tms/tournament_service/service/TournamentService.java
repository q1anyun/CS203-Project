package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.dto.PlayerRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateDTO;
import com.chess.tms.tournament_service.enums.RegistrationStatus;
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

    //helper function
    public TournamentDetailsDTO convertEntryToDTO(Tournament tournament) {
        return new TournamentDetailsDTO(tournament.getTournamentId(), tournament.getName(), tournament.getStartDate(), tournament.getEndDate(), 
                                        tournament.getMinElo(), tournament.getMaxElo(), tournament.getMaxPlayers(), 
                                        tournament.getMinPlayers(), tournament.getLocation(), tournament.getStatus(), 
                                        tournament.getType());
    }

    public PlayerRegistrationDTO convertPlayerEntrytoPlayerDTO(TournamentPlayer player) {
        PlayerRegistrationDTO dto = new PlayerRegistrationDTO();
        dto.setPlayerId(player.getPlayerId());
        dto.setRegistrationStatus(player.getRegistrationStatus());
        dto.setTournamentId(player.getTournament().getTournamentId());

        return dto;
    }

    public Tournament convertDTOToTournament(TournamentRegistrationDTO dto){
        Tournament tournament = new Tournament();
        tournament.setName(dto.getName());
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setMinElo(dto.getMinElo());
        tournament.setMaxElo(dto.getMaxElo());
        tournament.setMaxPlayers(dto.getMaxPlayers());
        tournament.setMinPlayers(dto.getMinPlayers());;
        tournament.setLocation(dto.getLocation());
        tournament.setStatus(dto.getStatus());
        tournament.setType(dto.getType());

        return tournament;
    }

    public TournamentDetailsDTO createTournament(TournamentRegistrationDTO dto) {
        
        // Map DTO to entry
        Tournament tournament = convertDTOToTournament(dto);
        //tournament.setId(tournamentRepository.count()+1);
        

        // Save in repo
        tournamentRepository.save(tournament);

        // Map saved entity to DTO and return
        TournamentDetailsDTO responseDTO = convertEntryToDTO(tournament);

        return responseDTO;                                                             
    }

    public TournamentDetailsDTO getTournamentDetailsById(long id) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        TournamentDetailsDTO returnDTO = convertEntryToDTO(tournament);
        return returnDTO;
    }

    public TournamentDetailsDTO deleteTournament(long id) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        TournamentDetailsDTO returnDTO = convertEntryToDTO(tournament);
        tournamentRepository.deleteById(id);
        return returnDTO;
    }

    public List<TournamentDetailsDTO> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentDetailsDTO> tournamentDTOs = new ArrayList<>();

        for (Tournament tournament : tournaments) {
            tournamentDTOs.add(convertEntryToDTO(tournament));
        }

        return tournamentDTOs;
    }

    public TournamentUpdateDTO updateTournament(long id, TournamentRegistrationDTO newTournament) {
        Tournament tournament = tournamentRepository.findById(id).get();
        if(tournament == null) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }


        TournamentDetailsDTO oldDTO = convertEntryToDTO(tournament);

        TournamentDetailsDTO updatedDTO = createTournament(newTournament);

        tournamentRepository.deleteById(id); 

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
            dtoList.add(convertPlayerEntrytoPlayerDTO(p));
        }

        return dtoList;
      
    }

    public List<PlayerRegistrationDTO> getAllPlayers() {
        List<PlayerRegistrationDTO> list = new ArrayList<>();

        for (TournamentPlayer p : tournamentPlayerRepository.findAll()) {
            list.add(convertPlayerEntrytoPlayerDTO(p));
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
