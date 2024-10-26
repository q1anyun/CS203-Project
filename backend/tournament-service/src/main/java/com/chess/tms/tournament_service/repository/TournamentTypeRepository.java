package com.chess.tms.tournament_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.chess.tms.tournament_service.model.TournamentType;

@Repository
public interface TournamentTypeRepository extends JpaRepository<TournamentType, Long> {
        public Optional<TournamentType> getTournamentTypeById(long id);
}
