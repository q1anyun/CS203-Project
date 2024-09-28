package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.GameType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTypeRepository extends JpaRepository<GameType, Long> {
    public GameType getGameTypeById(int id);
}