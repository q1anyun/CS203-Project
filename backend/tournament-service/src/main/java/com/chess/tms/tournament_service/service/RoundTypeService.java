package com.chess.tms.tournament_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.repository.RoundTypeRepository;

@Service
public class RoundTypeService {
    @Autowired
    private RoundTypeRepository roundTypeRepository;

    /**
     * Retrieves all available player count options for tournaments.
     * 
     * @return List of distinct player counts supported by the system
     */
    public List<Integer> getAvailablePlayerCounts() {
        return roundTypeRepository.findDistinctNumberOfPlayers();
    }
}