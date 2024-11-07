package com.chess.tms.tournament_service.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.service.TournamentService;

import jakarta.validation.Valid;

import java.util.List;




@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping("")
    public ResponseEntity<String> createTournament(@Valid @RequestBody TournamentRegistrationDTO dto, @RequestHeader("X-User-Id") String id) {
        return ResponseEntity.ok(tournamentService.createTournament(dto, Long.parseLong(id)));
    }

    @PostMapping("/start/{tournamentId}")
    public ResponseEntity<String> startTournament(@PathVariable("tournamentId") long tournamentId) {
        return ResponseEntity.ok(tournamentService.startTournament(tournamentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDetailsDTO> getTournament(@PathVariable("id") long id) {
        return ResponseEntity.ok(tournamentService.getTournamentDetailsById(id));
    }

    @GetMapping("")
    public ResponseEntity<List<TournamentDetailsDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.getAllTournaments());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<String> updateTournament(@PathVariable ("id") long id, @RequestBody TournamentUpdateRequestDTO updatedDTO) {
        tournamentService.updateTournament(id, updatedDTO);
        return ResponseEntity.ok("Tournament updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTournament(@PathVariable("id") long id) {
        tournamentService.deleteTournament(id);
        return ResponseEntity.ok("Successfully deleted tournament");
    }

    @PutMapping("/{id}/round/{round_type}")
    public ResponseEntity<String> updateCurrentRoundForTournament(@PathVariable ("id") long id, @PathVariable ("round_type") long roundType) {
        tournamentService.updateCurrentRoundForTournament(id, roundType);
        return ResponseEntity.ok("Current round updated to " + roundType);
    }

    @PutMapping("/{id}/winner/{winnerId}")
    public ResponseEntity<String> completeTournament(@PathVariable ("id") long id, @PathVariable ("winnerId") long winnerId) {
        String response= tournamentService.completeTournament(id, winnerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registered/{playerId}")
    public ResponseEntity<List<TournamentDetailsDTO>> getRegisteredTournaments(@PathVariable ("playerId") long playerId) {
        return ResponseEntity.ok(tournamentService.getRegisteredTournaments(playerId));
    }

    @GetMapping("/registered/current")
    public ResponseEntity<List<TournamentDetailsDTO>> getRegisteredTournaments( @RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(tournamentService.getRegisteredTournaments(playerId));
    }

    @GetMapping("/live/current")
    public ResponseEntity<List<TournamentDetailsDTO>> getLiveTournaments( @RequestHeader("X-User-PlayerId") String id) {
        long playerId = Long.parseLong(id);
        return ResponseEntity.ok(tournamentService.getLiveTournaments(playerId));
    }
}
