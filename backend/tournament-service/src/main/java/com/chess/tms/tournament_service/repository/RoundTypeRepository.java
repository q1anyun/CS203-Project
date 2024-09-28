package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.RoundType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    public RoundType getRoundTypeById(long id);
}