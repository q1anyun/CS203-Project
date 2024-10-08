package com.chess.tms.elo_service.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.enums.Reason;


@Repository
public interface EloRepository extends JpaRepository<EloHistory, Long>{
    public Optional<List<EloHistory>> findByPlayerId(long playerId);
    
    public List<EloHistory> findByCreatedAtLessThanEqual(LocalDateTime latest);

    public List<EloHistory> findByCreatedAtGreaterThanEqual(LocalDateTime earliest);

    public List<EloHistory> findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(long playerId, Reason changeReason);

    public void deleteByPlayerId(long playerId);

    public List<EloHistory> findByPlayerIdOrderByCreatedAtDesc(long playerId);

    public List<EloHistory> findByPlayerIdOrderByCreatedAtAsc(long playerId);

    public Optional<EloHistory> findByPlayerIdAndCreatedAt(long playerId, LocalDateTime createdAt);
    
}
