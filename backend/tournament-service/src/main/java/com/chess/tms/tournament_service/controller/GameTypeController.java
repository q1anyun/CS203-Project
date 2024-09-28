package com.chess.tms.tournament_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.service.GameTypeService;

@RestController
@RequestMapping("/api/game-type")
public class GameTypeController {

    @Autowired
    GameTypeService gameTypeService;

    @GetMapping("")
    public ResponseEntity<List<GameType>> getGameTypes() {
        return ResponseEntity.ok(gameTypeService.getGameTypes());
    }
}
