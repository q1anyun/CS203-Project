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

import java.util.List;




@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @PostMapping("")
    public ResponseEntity<TournamentDetailsDTO> registerTournament(@RequestBody TournamentRegistrationDTO dto, @RequestHeader("X-User-Id") String id) {
        return ResponseEntity.ok(tournamentService.createTournament(dto, Long.parseLong(id)));
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
    public ResponseEntity<TournamentDetailsDTO> deleteTournament(@PathVariable("id") long id) {
        return ResponseEntity.ok(tournamentService.deleteTournament(id));
    }
}
