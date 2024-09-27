package com.chess.tms.leaderboard_service.repository;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.leaderboard_service.model.LeaderboardEntry;


@Repository
public interface LeaderboardRepository extends JpaRepository<LeaderboardEntry, Long>{

    public List<LeaderboardEntry> findAll();

    public Optional<LeaderboardEntry> findById(long id);

    public Optional<LeaderboardEntry> findByPlayerId(long playerId);

    public List<LeaderboardEntry> findByElo(int elo);

    public List<LeaderboardEntry> findByEloBetween(int minElo, int maxElo);

    public List<LeaderboardEntry> findByEloGreaterThanEqual(int minElo);

    public List<LeaderboardEntry> findByEloLessThanEqual(int maxElo);

    public List<LeaderboardEntry> findByRankingGreaterThanEqual(int minRank);

    public List<LeaderboardEntry> findByRankingLessThanEqual(int maxRank);

    public List<LeaderboardEntry> findByRankingBetween (int minRank, int maxRank);

    public Optional<LeaderboardEntry> deleteByPlayerId(long playerId);
    
}
