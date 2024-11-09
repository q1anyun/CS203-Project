package com.chess.tms.tournament_service.unit.controller;

import com.chess.tms.tournament_service.controller.GameTypeController;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.service.GameTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameTypeController.class)
@ExtendWith(MockitoExtension.class)
class GameTypeControllerTest {

    private static final String BASE_URL = "/api/game-type";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameTypeService gameTypeService;

    @InjectMocks
    private GameTypeController gameTypeController;

    private List<GameType> testGameTypes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameTypeController).build();
        
        // Initialize test data
        testGameTypes = Arrays.asList(
            createGameType(1L, "Blitz", 5),
            createGameType(2L, "Rapid", 15)
        );
    }

    private GameType createGameType(Long id, String name, int timeControlMinutes) {
        GameType gameType = new GameType();
        gameType.setId(id);
        gameType.setName(name);
        gameType.setTimeControlMinutes(timeControlMinutes);
        return gameType;
    }

    @Test
    void getGameTypes_Valid_ReturnListOfGameTypes() throws Exception {
        // Given
        when(gameTypeService.getGameTypes()).thenReturn(testGameTypes);

        // When/Then
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testGameTypes.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(testGameTypes.get(0).getName()))
                .andExpect(jsonPath("$[0].timeControlMinutes").value(testGameTypes.get(0).getTimeControlMinutes()))
                .andExpect(jsonPath("$[1].id").value(testGameTypes.get(1).getId()))
                .andExpect(jsonPath("$[1].name").value(testGameTypes.get(1).getName()))
                .andExpect(jsonPath("$[1].timeControlMinutes").value(testGameTypes.get(1).getTimeControlMinutes()));

        verify(gameTypeService).getGameTypes();
    }
}