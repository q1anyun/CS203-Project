package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.GameType;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameTypeRepository extends JpaRepository<GameType, Long> {
    /**
     * Retrieves a GameType entity by its ID.
     * 
     * @param id The unique identifier of the GameType
     * @return Optional containing the GameType if found, empty Optional otherwise
     */
    public Optional<GameType> getGameTypeById(long id);
}