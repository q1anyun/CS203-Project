package com.chess.tms.tournament_service.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.tournament_service.repository.RoundTypeRepository;

@Service
public class RoundTypeService {
    @Autowired
    private RoundTypeRepository roundTypeRepository;

    public List<Integer> getChoicesForNumberOfPlayers() {
        return roundTypeRepository.getAllNumberOfPlayers();
    }
}
