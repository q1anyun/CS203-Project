package com.chess.tms.player_analytics_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.player_analytics_service.model.PlayerDetails;

public interface PlayerDetailsRepository extends JpaRepository<PlayerDetails, Long> {
    Optional<PlayerDetails> findByUserId(Long userId);
}