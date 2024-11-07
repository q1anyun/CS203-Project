package com.chess.tms.tournament_service.unit.service;

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
public class GameTypeServiceTest {

    @Mock
    private GameTypeRepository gameTypeRepository;

    @InjectMocks
    private GameTypeService gameTypeService;

    private List<GameType> expectedGameTypes;

    @BeforeEach
    void setup() {
        expectedGameTypes = List.of(
                createGameType(1L, "Blitz", 5),
                createGameType(2L, "Rapid", 15)
        );
    }

    @Test
    void shouldReturnListOfGameTypes() {
        // Arrange: Mock repository to return predefined game types
        when(gameTypeRepository.findAll()).thenReturn(expectedGameTypes);

        // Act: Fetch game types from the service
        List<GameType> actualGameTypes = gameTypeService.getGameTypes();

        // Assert: Validate the result
        assertGameTypes(expectedGameTypes, actualGameTypes);
    }

    // Helper method to reduce repetition when creating GameType objects
    private GameType createGameType(Long id, String name, int timeControlMinutes) {
        GameType gameType = new GameType();
        gameType.setId(id);
        gameType.setName(name);
        gameType.setTimeControlMinutes(timeControlMinutes);
        return gameType;
    }

    // Helper method for asserting game type lists
    private void assertGameTypes(List<GameType> expected, List<GameType> actual) {
        assertEquals(expected.size(), actual.size(), "Game types size should match.");
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.get(i).getId(), actual.get(i).getId(), "GameType ID at index " + i + " should match.");
            assertEquals(expected.get(i).getName(), actual.get(i).getName(), "GameType name at index " + i + " should match.");
            assertEquals(expected.get(i).getTimeControlMinutes(), actual.get(i).getTimeControlMinutes(),
                    "GameType time control at index " + i + " should match.");
        }
    }
}
