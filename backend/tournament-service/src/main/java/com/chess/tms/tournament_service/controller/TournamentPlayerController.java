package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.PlayerRegistrationDTO;
import com.chess.tms.tournament_service.service.TournamentService;


@RestController
@RequestMapping("/api/tournamentplayers")
public class TournamentPlayerController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/{id}")
    public ResponseEntity<List<PlayerDetailsDTO>> getTournamentPlayersByTournamentId(@PathVariable("id") long id) {
        return ResponseEntity.ok(tournamentService.getPlayersByTournament(id));
    }

    // @GetMapping("")
    // public ResponseEntity<List<PlayerRegistrationDTO>> getAllPlayers() {
    //     return ResponseEntity.ok(tournamentService.getAllPlayers());
    // }

    @DeleteMapping("/{playerid}/{tournamentid}")
    public ResponseEntity<String> deletePlayerFromTournament(@PathVariable long playerid, @PathVariable long tournamentid) {
        tournamentService.deletePlayerFromTournament(playerid, tournamentid);
        return ResponseEntity.ok("Player deleted successfully");
    }

    @PostMapping("/register/{playerid}/{tournamentid}") 
    public ResponseEntity<String> registerPlayer(@PathVariable long playerid, @PathVariable long tournamentid) {
        tournamentService.registerPlayer(playerid, tournamentid);
        return ResponseEntity.ok("Player registered successfully");
    }
}
