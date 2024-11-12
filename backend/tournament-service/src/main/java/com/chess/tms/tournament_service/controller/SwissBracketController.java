package com.chess.tms.tournament_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.tournament_service.dto.SwissBracketDTO;
import com.chess.tms.tournament_service.service.SwissBracketService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/swiss-bracket")
public class SwissBracketController {
    @Autowired
    private SwissBracketService swissBracketService;

    @GetMapping("/{tournamentId}")
    public SwissBracketDTO getSwissBracket(@PathVariable("tournamentId") long tournamentId) {
        return swissBracketService.getSwissBracket(tournamentId);
    }
}
