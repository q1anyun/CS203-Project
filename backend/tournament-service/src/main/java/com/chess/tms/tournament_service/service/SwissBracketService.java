package com.chess.tms.tournament_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.dto.SwissBracketDTO;
import com.chess.tms.tournament_service.exception.SwissBracketNotFoundException;
import com.chess.tms.tournament_service.repository.SwissBracketRepository;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.repository.TournamentRepository;
@Service
public class SwissBracketService {
    @Autowired
    private SwissBracketRepository swissBracketRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    /**
     * Retrieves a Swiss bracket by its ID and converts it to DTO
     * @param id The ID of the Swiss bracket to retrieve
     * @return SwissBracketDTO containing the bracket information
     * @throws SwissBracketNotFoundException if bracket is not found
     */
    public SwissBracketDTO getSwissBracket(Long tournamentId) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentId);
        return swissBracketRepository.findByTournament(tournament.get())
            .map(bracket -> {
                SwissBracketDTO dto = new SwissBracketDTO();
                dto.setId(bracket.getId());
                dto.setCurrentRound(bracket.getCurrentRound());
                dto.setNumberOfRounds(bracket.getNumberOfRounds());
                return dto;
            })
            .orElseThrow(() -> new SwissBracketNotFoundException("Bracket not found"));
    }
}