package com.chess.tms.tournament_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.repository.GameTypeRepository;

@Service
public class GameTypeService {
    @Autowired
    private GameTypeRepository gameTypeRepository;

    /**
     * Retrieves all game types from the repository
     * @return List of all game types
     */
    public List<GameType> getGameTypes() {
        return gameTypeRepository.findAll();
    }
}