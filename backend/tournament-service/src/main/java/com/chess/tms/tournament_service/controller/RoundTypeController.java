package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.tournament_service.service.RoundTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/round-type")
public class RoundTypeController {
    @Autowired
    private RoundTypeService roundTypeService;

    @GetMapping("/choices")
    public List<Integer> getChoicesForNumberOfPlayers(@RequestParam String param) {
        return roundTypeService.getChoicesForNumberOfPlayers();
    }
}
