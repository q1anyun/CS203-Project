package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.service.TournamentService;


@RestController
@RequestMapping("/api/tournament-players")
public class TournamentPlayerController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/{id}")
    public ResponseEntity<List<PlayerDetailsDTO>> getTournamentPlayersByTournamentId(@PathVariable("id") long id) {
        return ResponseEntity.ok(tournamentService.getPlayersByTournament(id));
    }

    @DeleteMapping("/{playerid}/{tournamentid}")
    public ResponseEntity<String> deletePlayerFromTournament(@PathVariable("playerid") long playerid, @PathVariable("tournamentid") long tournamentid) {
        tournamentService.deletePlayerFromTournament(playerid, tournamentid);
        return ResponseEntity.ok("Player deleted successfully");
    }

    @PostMapping("/register/{playerid}/{tournamentid}") 
    public ResponseEntity<String> registerPlayer(@PathVariable("playerid") long playerid, @PathVariable("tournamentid") long tournamentid) {
        tournamentService.registerPlayer(playerid, tournamentid);
        return ResponseEntity.ok("Player registered successfully");
    }

    @DeleteMapping("/current/{tournamentid}")
    public ResponseEntity<String> deleteCurrentPlayerFromTournament(@RequestHeader("X-User-PlayerId") String id, @PathVariable("tournamentid") long tournamentid) {
        Long playerId = Long.parseLong(id);
        tournamentService.deletePlayerFromTournament(playerId, tournamentid);
        return ResponseEntity.ok("Player deleted successfully");
    }

    @PostMapping("/register/current/{tournamentid}") 
    public ResponseEntity<String> registerCurrentPlayer(@RequestHeader("X-User-PlayerId") String id, @PathVariable("tournamentid") long tournamentid) {
        Long playerId = Long.parseLong(id);
        tournamentService.registerPlayer(playerId, tournamentid);
        return ResponseEntity.ok("Player registered successfully");
    }
}
