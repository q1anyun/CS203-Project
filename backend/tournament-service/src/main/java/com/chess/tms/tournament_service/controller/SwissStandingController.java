package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.tms.tournament_service.service.SwissStandingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.chess.tms.tournament_service.dto.SwissStandingDTO;

@RestController
@RequestMapping("/api/swiss-standing")
public class SwissStandingController {
    @Autowired
    private SwissStandingService swissStandingService;

    @GetMapping("/{id}")
    public List<SwissStandingDTO> getSwissStandings(@PathVariable("id") long id) {
        return swissStandingService.getSwissStandings(id);
    }
}
