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

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameTypeService gameTypeService;

    @InjectMocks
    private GameTypeController gameTypeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameTypeController).build();
    }

    @Test
    void getGameTypes_Valid_ReturnListOfGameTypes() throws Exception {
        GameType gameType1 = new GameType();
        gameType1.setId(1L);
        gameType1.setName("Blitz");
        gameType1.setTimeControlMinutes(5);

        GameType gameType2 = new GameType();
        gameType2.setId(2L);
        gameType2.setName("Rapid");
        gameType2.setTimeControlMinutes(15);

        List<GameType> gameTypes = Arrays.asList(gameType1, gameType2);

        when(gameTypeService.getGameTypes()).thenReturn(gameTypes);

        mockMvc.perform(get("/api/game-type")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Blitz"))
                .andExpect(jsonPath("$[0].timeControlMinutes").value(5))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Rapid"))
                .andExpect(jsonPath("$[1].timeControlMinutes").value(15));

        verify(gameTypeService, times(1)).getGameTypes();
    }
}