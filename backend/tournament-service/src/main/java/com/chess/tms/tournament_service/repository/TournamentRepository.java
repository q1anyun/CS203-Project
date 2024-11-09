package com.chess.tms.tournament_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;   
import com.chess.tms.tournament_service.model.Tournament;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;


public interface TournamentRepository extends JpaRepository<Tournament, Long>{
    /**
     * Finds a tournament by its unique identifier
     * @param id The tournament's unique identifier
     * @return Optional containing the tournament if found, empty otherwise
     */
    public Optional<Tournament> findById(long id);

    /**
     * Deletes a tournament by its unique identifier
     * @param id The tournament's unique identifier
     */
    public void deleteById(long id);

    /**
     * Finds a tournament by matching exact name, start date, and end date
     * @param name The tournament name
     * @param startDate The tournament start date and time
     * @param endDate The tournament end date and time
     * @return Optional containing the tournament if found, empty otherwise
     */
    public Optional<Tournament> findByNameAndStartDateAndEndDate(String name, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds all tournaments with a matching name
     * @param name The tournament name to search for
     * @return List of tournaments matching the name
     */
    public List<Tournament> findByName(String name);

    /**
     * Retrieves all tournaments in the database
     * @return List of all tournaments
     */
    public List<Tournament> findAll();
    
    /**
     * Finds all tournaments starting at a specific date and time
     * @param startDate The start date and time to search for
     * @return List of tournaments matching the start date
     */
    public List<Tournament> findByStartDate(LocalDateTime startDate);

    /**
     * Finds all tournaments ending at a specific date and time
     * @param endDate The end date and time to search for
     * @return List of tournaments matching the end date
     */
    public List<Tournament> findByEndDate(LocalDateTime endDate);
}