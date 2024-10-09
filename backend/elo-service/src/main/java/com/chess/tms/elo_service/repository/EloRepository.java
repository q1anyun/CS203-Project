package com.chess.tms.elo_service.repository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

@Query(value = "SELECT * FROM elo_history e WHERE e.player_id = :playerId AND e.created_at IN " +
       "(SELECT MAX(eh.created_at) FROM elo_history eh WHERE eh.player_id = :playerId " +
       "GROUP BY DATE(eh.created_at)) " +
       "ORDER BY e.created_at DESC LIMIT 5", nativeQuery = true)
List<EloHistory> findLatestEloHistoryByPlayerId(@Param("playerId") Long playerId);
    
}
