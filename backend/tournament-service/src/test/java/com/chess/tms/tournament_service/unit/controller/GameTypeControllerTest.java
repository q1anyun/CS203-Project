package com.chess.tms.tournament_service.unit.controller;

import com.chess.tms.tournament_service.controller.GameTypeController;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.service.GameTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameTypeController.class)
@ExtendWith(MockitoExtension.class)
class GameTypeControllerTest {

    private GameType createGameType(Long id, String name, int timeControlMinutes) {
        GameType gameType = new GameType();
        gameType.setId(id);
        gameType.setName(name);
        gameType.setTimeControlMinutes(timeControlMinutes);
        return gameType;
    }

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private GameTypeService gameTypeService;

    @InjectMocks
    private GameTypeController gameTypeController;

    private GameType gameType1;
    private GameType gameType2;

    @BeforeEach
    void setUp() {
        // Set up test data
        gameType1 = createGameType(1L, "Blitz", 5);
        gameType2 = createGameType(2L, "Rapid", 15);

        mockMvc = MockMvcBuilders.standaloneSetup(gameTypeController).build();
    }

    @Test
    void getGameTypes_Valid_ReturnListOfGameTypes() throws Exception {
        List<GameType> gameTypes = List.of(gameType1, gameType2);

        when(gameTypeService.getGameTypes()).thenReturn(gameTypes);

        mockMvc.perform(get("/api/game-type")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(gameType1.getId()))
                .andExpect(jsonPath("$[0].name").value(gameType1.getName()))
                .andExpect(jsonPath("$[0].timeControlMinutes").value(gameType1.getTimeControlMinutes()))
                .andExpect(jsonPath("$[1].id").value(gameType2.getId()))
                .andExpect(jsonPath("$[1].name").value(gameType2.getName()))
                .andExpect(jsonPath("$[1].timeControlMinutes").value(gameType2.getTimeControlMinutes()));

        verify(gameTypeService, times(1)).getGameTypes();
    }


}
