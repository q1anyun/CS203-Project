package com.chess.tms.match_service.repository;

import com.chess.tms.match_service.model.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    Optional<RoundType> findByNumberOfPlayers(int numberOfPlayers);
}
