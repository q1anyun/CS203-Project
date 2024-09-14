package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateDTO;
import com.chess.tms.tournament_service.repository.TournamentRepository;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.model.Tournament;


@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    //helper function
    public TournamentDetailsDTO convertEntryToDTO(Tournament tournament) {
        return new TournamentDetailsDTO(tournament.getId(), tournament.getName(), tournament.getStartDate(), tournament.getEndDate(), 
                                        tournament.getMinElo(), tournament.getMaxElo(), tournament.getMaxPlayers(), 
                                        tournament.getMinPlayers(), tournament.getLocation(), tournament.getStatus(), 
                                        tournament.getType());
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
}
