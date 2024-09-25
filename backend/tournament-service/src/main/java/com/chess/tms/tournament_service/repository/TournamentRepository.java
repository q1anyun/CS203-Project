package com.chess.tms.tournament_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;   
import com.chess.tms.tournament_service.model.Tournament;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


public interface TournamentRepository extends JpaRepository<Tournament, Long>{

    public Optional<Tournament> findById(long id);

    public void deleteById(long id);

    public Optional<Tournament> findByNameAndStartDateAndEndDate(String name, LocalDateTime startDate, LocalDateTime endDate);

    public List<Tournament> findByName(String name);

    public List<Tournament> findAll();
    
    public List<Tournament> findByStartDate(LocalDateTime startDate);

    public List<Tournament> findByEndDate(LocalDateTime endDate);
 
}