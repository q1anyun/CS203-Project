package com.chess.tms.elo_service.unit.eloController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.chess.tms.elo_service.controller.EloController;
import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.exception.GlobalExceptionHandler;
import com.chess.tms.elo_service.service.EloService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.chess.tms.elo_service.enums.Reason;


@ExtendWith(MockitoExtension.class)
public class EloControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EloService eloService;

    @InjectMocks
    private EloController eloController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(eloController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
    }

    @Test
    void testSaveEloHistorySuccess() throws Exception {
        String jsonPayload = """
            {
                "playerId": 1,
                "newElo": 1315,
                "changeReason": "WIN"
            }
                """;
        LocalDateTime timeNow = LocalDateTime.parse("2024-10-10T16:02:18");
        EloResponseDTO response = new EloResponseDTO(1, 1315, 1315, Reason.WIN, timeNow);

            when(eloService.saveEloHistory(1315, any(EloHistoryRequestDTO.class)))
                .thenReturn(response);
            // serialize expected response to json
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String expectedResponseJson = objectMapper.writeValueAsString(response);

            mockMvc.perform(post("/api/elo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)
                .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseJson));
                
            verify(eloService, times(1)).saveEloHistory(1315, any(EloHistoryRequestDTO.class));
    }

    @Test
    void testDeleteEloHistory() throws Exception {
        // EloHistoryRequestDTO dto = new EloHistoryRequestDTO(1, 1315, Reason.WIN);
        // eloService.saveEloHistory(dto);
        LocalDateTime timeNow = LocalDateTime.parse("2024-10-10T16:02:18");
        List<EloResponseDTO> deleted = new ArrayList<>();
        deleted.add(new EloResponseDTO(1, 1315, 1315, Reason.LOSS, timeNow));

         // serialize expected response to json
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.registerModule(new JavaTimeModule());
         String expectedResponseJson = objectMapper.writeValueAsString(deleted);

        when (eloService.deleteByPlayerId(1))
            .thenReturn(deleted);
        
        mockMvc.perform(delete("/api/elo/delete/1"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedResponseJson));

        verify(eloService, times(1)).deleteByPlayerId(eq(1L));
    }

    @Test 
    void testFindAllByPlayerIdSuccess() throws Exception{
        List<EloResponseDTO> list =  new ArrayList<>();
        LocalDateTime timeNow = LocalDateTime.parse("2024-10-10T16:02:18");
        list.add(new EloResponseDTO(1L, 1315, 1315, Reason.WIN, timeNow));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String expectedResponseJson = objectMapper.writeValueAsString(list);

        when(eloService.findEloHistoryByPlayerId(1L))
            .thenReturn(list);
        
        mockMvc.perform(get("/api/elo/1"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseJson));

        verify(eloService, times(1)).findEloHistoryByPlayerId(1L);
    }

    @Test
    void testFindByPlayerIdAndChangeReason() throws Exception{
        List<EloResponseDTO> list =  new ArrayList<>();
        LocalDateTime timeNow = LocalDateTime.parse("2024-10-10T16:02:18");
        list.add(new EloResponseDTO(1L, 1315, 1315, Reason.WIN, timeNow));

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String expectedResponseJson = objectMapper.writeValueAsString(list);

        when(eloService.findByPlayerIdAndChangeReason(1L, "win"))
            .thenReturn(list);
        
        mockMvc.perform(get("/api/elo/1/win"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseJson));

        verify(eloService, times(1)).findByPlayerIdAndChangeReason(1L, "win");
    }

    @Test
    void testUpdateMatchPlayersEloSuccess() throws Exception {

        String jsonPayload = """
            {
                "playerId": 1,
                "newElo": 1315
            },
            {
                "playerId": 2,
                "newElo": 1320
            }
                """;
        
        mockMvc.perform(put("/api/elo/match")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonPayload)
        .header("X-User-Id", "1"))
        .andExpect(status().isOk());

        verify(eloService, times(1)).updateMatchPlayersElo(any(MatchEloRequestDTO.class));
    }

    @Test
    void testFindCurrentPlayerEloHistoryForChart() throws Exception {
        List<EloHistoryChartDTO> list = new ArrayList<>();
        LocalDate t1 = LocalDate.now();
        LocalDate t2 = LocalDate.now();
        
        list.add(new EloHistoryChartDTO(1385, t1));
        list.add(new EloHistoryChartDTO(1285, t2));

        when(eloService.findPlayerEloHistoryForChart(1L))
            .thenReturn(list);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String expectedResponseJson = objectMapper.writeValueAsString(list);
        
        mockMvc.perform(get("/api/elo/chart/current")
        .contentType(MediaType.APPLICATION_JSON)
        .content("")
        .header("X-User-PlayerId", "1"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseJson));

        verify(eloService, times(1)).findPlayerEloHistoryForChart(1L);
    }

    @Test
    void testFindPlayerEloHistoryForChart() throws Exception {
        List<EloHistoryChartDTO> list = new ArrayList<>();

        list.add(new EloHistoryChartDTO(1315, LocalDate.parse("2024-10-10")));
        list.add(new EloHistoryChartDTO(1320, LocalDate.parse("2024-02-03")));

        when(eloService.findPlayerEloHistoryForChart(1L))
            .thenReturn(list);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String expectedResponseJson = objectMapper.writeValueAsString(list);
        
        mockMvc.perform(get("/api/elo/chart/1"))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedResponseJson));
        
        verify(eloService, times(1)).findPlayerEloHistoryForChart(1L);
    }





}
