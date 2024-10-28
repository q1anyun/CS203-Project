package com.chess.tms.tournament_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.dto.SwissBracketDTO;
import com.chess.tms.tournament_service.exception.SwissBracketNotFoundException;
import com.chess.tms.tournament_service.model.SwissBracket;
import com.chess.tms.tournament_service.repository.SwissBracketRepository;

@Service
public class SwissBracketService {
    @Autowired
    private SwissBracketRepository swissBracketRepository;

    public SwissBracketDTO getSwissBracket(Long id) {
        Optional<SwissBracket> bracket = swissBracketRepository.findById(id);

        // Throw exception if bracket is not found
        if (!bracket.isPresent()) {
            throw new SwissBracketNotFoundException("Bracket not found");
        }
            SwissBracketDTO dto = new SwissBracketDTO();
            dto.setCurrentRound(bracket.get().getCurrentRound());
            dto.setNumberOfRounds(bracket.get().getNumberOfRounds());
            return dto;

    }
}
