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

    public List<GameType> getGameTypes() {
        return gameTypeRepository.getAllGameType();
    }

}
