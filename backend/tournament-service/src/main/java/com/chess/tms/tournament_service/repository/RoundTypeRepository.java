package com.chess.tms.tournament_service.repository;

import com.chess.tms.tournament_service.model.RoundType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoundTypeRepository extends JpaRepository<RoundType, Long> {
    @Query("SELECT DISTINCT r.numberOfPlayers FROM RoundType r")
    public List<Integer> findDistinctNumberOfPlayers();
}