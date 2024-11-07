package com.chess.tms.tournament_service.unit.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
class GameTypeServiceTest {

    @Mock
    private GameTypeRepository gameTypeRepository;

    @InjectMocks
    private GameTypeService gameTypeService;

    private List<GameType> gameTypes;

    @BeforeEach
    void setup() {
        gameTypes = List.of(
            createGameType(1L, "Blitz", 5),
            createGameType(2L, "Rapid", 15)
        );
    }

    @Test
    void getGameTypes_ReturnsListOfGameTypes() {
        when(gameTypeRepository.findAll()).thenReturn(gameTypes);

        List<GameType> result = gameTypeService.getGameTypes();

        assertAll("gameTypes",
            () -> assertEquals(2, result.size(), "Size should match"),
            () -> assertEquals("Blitz", result.get(0).getName(), "First game type name"),
            () -> assertEquals(5, result.get(0).getTimeControlMinutes(), "First game type time control"),
            () -> assertEquals("Rapid", result.get(1).getName(), "Second game type name"),
            () -> assertEquals(15, result.get(1).getTimeControlMinutes(), "Second game type time control")
        );
    }

    private GameType createGameType(Long id, String name, int timeControlMinutes) {
        GameType gameType = new GameType();
        gameType.setId(id);
        gameType.setName(name);
        gameType.setTimeControlMinutes(timeControlMinutes);
        return gameType;
    }
}
