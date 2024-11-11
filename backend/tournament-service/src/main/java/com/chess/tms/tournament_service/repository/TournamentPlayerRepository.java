package com.chess.tms.tournament_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import java.util.List;
import java.util.Optional;


public interface TournamentPlayerRepository extends JpaRepository<TournamentPlayer, Long> {
    /**
     * Finds all tournament entries for a specific player.
     * @param playerId The ID of the player
     * @return List of tournament player entries
     */
    public List<TournamentPlayer> findByPlayerId(long playerId);

    /**
     * Finds a specific tournament entry for a player in a given tournament.
     * @param playerId The ID of the player
     * @param tournament The tournament object
     * @return Optional containing the tournament player entry if found
     */
    public Optional<TournamentPlayer> findByPlayerIdAndTournament(long playerId, Tournament tournament);

    /**
     * Retrieves all players registered in a specific tournament.
     * @param tournament The tournament object
     * @return List of tournament player entries
     */
    public List<TournamentPlayer> findAllByTournament(Tournament tournament);

    /**
     * Retrieves all tournament player entries in the system.
     * @return List of all tournament player entries
     */
    public List<TournamentPlayer> findAll();

    /**
     * Deletes all tournament entries for a specific player.
     * @param playerId The ID of the player to delete entries for
     */
    public void deleteByPlayerId(long playerId);

    /**
     * Finds all tournament entries for a specific player.
     * @param playerId The ID of the player
     * @return List of tournament player entries
     */
    public List<TournamentPlayer> findAllByPlayerId(long playerId);

    /**
     * Finds a specific tournament entry for a player using tournament ID.
     * @param playerId The ID of the player
     * @param tournamentId The ID of the tournament
     * @return Optional containing the tournament player entry if found
     */
    public Optional<TournamentPlayer> findByPlayerIdAndTournament_TournamentId(long playerId, long tournamentId);
}
