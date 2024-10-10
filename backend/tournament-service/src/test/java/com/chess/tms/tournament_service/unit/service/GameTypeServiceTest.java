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

    private List<GameType> gameTypes;

    @BeforeEach
    void setup() {
        GameType gameType1 = new GameType();
        gameType1.setId(1L);
        gameType1.setName("Blitz");
        gameType1.setTimeControlMinutes(5);

        GameType gameType2 = new GameType();
        gameType2.setId(2L);
        gameType2.setName("Rapid");
        gameType2.setTimeControlMinutes(15);

        gameTypes = Arrays.asList(gameType1, gameType2);
    }

    @Test
    void testGetGameTypes() {
        when(gameTypeRepository.findAll()).thenReturn(gameTypes);

        List<GameType> result = gameTypeService.getGameTypes();

        assertEquals(2, result.size());
        assertEquals("Blitz", result.get(0).getName());
        assertEquals(5, result.get(0).getTimeControlMinutes());
        assertEquals("Rapid", result.get(1).getName());
        assertEquals(15, result.get(1).getTimeControlMinutes());
    }
}