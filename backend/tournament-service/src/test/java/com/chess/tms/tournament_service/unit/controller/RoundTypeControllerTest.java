package com.chess.tms.tournament_service.unit.controller;

import com.chess.tms.tournament_service.controller.RoundTypeController;
import com.chess.tms.tournament_service.service.RoundTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoundTypeController.class)
@ExtendWith(MockitoExtension.class)
class RoundTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoundTypeService roundTypeService;

    @InjectMocks
    private RoundTypeController roundTypeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roundTypeController).build();
    }

    @Test
    void getChoicesForNumberOfPlayers_ReturnsValidChoicesList() throws Exception {
        List<Integer> choices = List.of(2, 4, 8, 16, 32, 64);

        when(roundTypeService.getChoicesForNumberOfPlayers()).thenReturn(choices);

        mockMvc.perform(get("/api/round-type/choices")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[2,4,8,16,32,64]"));

        verify(roundTypeService).getChoicesForNumberOfPlayers();
    }
}
