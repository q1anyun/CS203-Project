package com.chess.tms.tournament_service.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.service.GameTypeService;

@ExtendWith(MockitoExtension.class)
public class GameTypeServiceTest {

    @Mock
    private GameTypeRepository gameTypeRepository;

    @InjectMocks
    private GameTypeService gameTypeService;

    private static final String BLITZ_NAME = "Blitz";
    private static final String RAPID_NAME = "Rapid";
    private static final int BLITZ_TIME = 5;
    private static final int RAPID_TIME = 15;

    private GameType blitzGameType;
    private GameType rapidGameType;
    private List<GameType> gameTypes;

    @BeforeEach
    void setup() {
        blitzGameType = createGameType(1L, BLITZ_NAME, BLITZ_TIME);
        rapidGameType = createGameType(2L, RAPID_NAME, RAPID_TIME);
        gameTypes = Arrays.asList(blitzGameType, rapidGameType);
    }

    private GameType createGameType(Long id, String name, int timeControl) {
        GameType gameType = new GameType();
        gameType.setId(id);
        gameType.setName(name);
        gameType.setTimeControlMinutes(timeControl);
        return gameType;
    }

    @Test
    void getGameTypes_Valid_ReturnListOfGameTypes() {
        when(gameTypeRepository.findAll()).thenReturn(gameTypes);

        List<GameType> result = gameTypeService.getGameTypes();

        assertEquals(2, result.size());
        assertGameType(result.get(0), BLITZ_NAME, BLITZ_TIME);
        assertGameType(result.get(1), RAPID_NAME, RAPID_TIME);
    }

    private void assertGameType(GameType gameType, String expectedName, int expectedTime) {
        assertEquals(expectedName, gameType.getName());
        assertEquals(expectedTime, gameType.getTimeControlMinutes());
    }
}