package com.chess.tms.match_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.match_service.model.GameType;

public interface GameTypeRepository extends JpaRepository<GameType, Long>{

    
}