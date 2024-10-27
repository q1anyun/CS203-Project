package com.chess.tms.tournament_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.model.TournamentType;
import com.chess.tms.tournament_service.repository.TournamentTypeRepository;

@Service
public class TournamentTypeService {
    @Autowired
    private TournamentTypeRepository tournamentTypeRepository;

    public List<TournamentType> getTournamentTypes() {
        return tournamentTypeRepository.findAll();
    }
}
