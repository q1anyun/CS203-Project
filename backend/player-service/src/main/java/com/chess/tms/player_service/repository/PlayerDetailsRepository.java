package com.chess.tms.player_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.player_service.model.PlayerDetails;

public interface PlayerDetailsRepository extends JpaRepository<PlayerDetails, Long> {
    Optional<PlayerDetails> findByUserId(Long userId);
    List<PlayerDetails> findByIdIn(List<Long> playerIds);
}