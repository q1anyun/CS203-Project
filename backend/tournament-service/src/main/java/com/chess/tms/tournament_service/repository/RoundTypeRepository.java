package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.RoundType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    public RoundType getRoundTypeById();
    public List<Integer> getAllNumberOfPlayers();
}