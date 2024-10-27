package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.tms.tournament_service.model.TournamentType;
import com.chess.tms.tournament_service.service.TournamentTypeService;

@RestController
@RequestMapping("/api/tournament-type")
public class TournamentTypeController {

    @Autowired
    TournamentTypeService tournamentTypeService;

    @GetMapping("")
    public ResponseEntity<List<TournamentType>> getTournamentTypes() {
        return ResponseEntity.ok(tournamentTypeService.getTournamentTypes());
    }
}
