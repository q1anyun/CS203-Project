package com.chess.tms.tournament_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.dto.SwissBracketDTO;
import com.chess.tms.tournament_service.exception.SwissBracketNotFoundException;
import com.chess.tms.tournament_service.repository.SwissBracketRepository;
@Service
public class SwissBracketService {
    @Autowired
    private SwissBracketRepository swissBracketRepository;

    /**
     * Retrieves a Swiss bracket by its ID and converts it to DTO
     * @param id The ID of the Swiss bracket to retrieve
     * @return SwissBracketDTO containing the bracket information
     * @throws SwissBracketNotFoundException if bracket is not found
     */
    public SwissBracketDTO getSwissBracket(Long id) {
        return swissBracketRepository.findById(id)
            .map(bracket -> {
                SwissBracketDTO dto = new SwissBracketDTO();
                dto.setCurrentRound(bracket.getCurrentRound());
                dto.setNumberOfRounds(bracket.getNumberOfRounds());
                return dto;
            })
            .orElseThrow(() -> new SwissBracketNotFoundException("Bracket not found"));
    }
}