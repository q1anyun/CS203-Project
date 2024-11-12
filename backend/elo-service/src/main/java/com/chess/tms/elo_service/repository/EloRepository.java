package com.chess.tms.elo_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.model.EloHistory;

/** 
 * JPA repository representing the elo_history table in the SQL DB
 */
@Repository
public interface EloRepository extends JpaRepository<EloHistory, Long>{

    public List<EloHistory> findAll();

    public Optional<List<EloHistory>> findByPlayerId(long playerId);
    
    public List<EloHistory> findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(long playerId, Reason reason);

    public void deleteByPlayerId(long playerId);

    /**
     * This method returns the latest EloHistories of the latest 5 days for a given player id.
     * @param playerId
     */
    @Query(value = "SELECT * FROM elo_history e WHERE e.player_id = :playerId AND e.created_at IN " +
        "(SELECT MAX(eh.created_at) FROM elo_history eh WHERE eh.player_id = :playerId " +
        "GROUP BY DATE(eh.created_at)) " +
        "ORDER BY e.created_at DESC LIMIT 5", nativeQuery = true)
    List<EloHistory> findLatestEloHistoryByPlayerId(@Param("playerId") Long playerId);

            
    //public List<EloHistory> findByCreatedAtLessThanEqual(LocalDateTime latestTime);

    //public List<EloHistory> findByCreatedAtGreaterThanEqual(LocalDateTime earliestTime);

    //public List<EloHistory> findByPlayerIdOrderByCreatedAtDesc(long playerId);

    //public List<EloHistory> findByPlayerIdOrderByCreatedAtAsc(long playerId);
    }
