package com.chess.tms.player_service.unit.controller;

import com.chess.tms.player_service.controller.PlayerController;
import com.chess.tms.player_service.dto.*;
import com.chess.tms.player_service.exception.GlobalExceptionHandler;
import com.chess.tms.player_service.service.PlayerService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayerControllerTest {

    @InjectMocks
    private PlayerController playerController;

    @Mock
    private PlayerService playerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(playerController)
        .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void getAllPlayers_ReturnsPlayerList() throws Exception {
        List<PlayerDetailsDTO> players = Arrays.asList(new PlayerDetailsDTO(), new PlayerDetailsDTO());
        when(playerService.getAllPlayers()).thenReturn(players);

        mockMvc.perform(get("/api/player"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(players)));

        verify(playerService).getAllPlayers();
    }

    @Test
    void getCurrentPlayerDetails_ReturnsPlayerDetails() throws Exception {
        long playerId = 1L;
        PlayerDetailsDTO playerDetails = new PlayerDetailsDTO();
        when(playerService.getPlayerDetailsById(playerId)).thenReturn(playerDetails);

        mockMvc.perform(get("/api/player/currentPlayerById")
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(playerDetails)));

        verify(playerService).getPlayerDetailsById(playerId);
    }

    @Test
    void getPlayerDetailsById_ReturnsPlayerDetails() throws Exception {
        long playerId = 1L;
        PlayerDetailsDTO playerDetails = new PlayerDetailsDTO();
        when(playerService.getPlayerDetailsById(playerId)).thenReturn(playerDetails);

        mockMvc.perform(get("/api/player/{id}", playerId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(playerDetails)));

        verify(playerService).getPlayerDetailsById(playerId);
    }

    @Test
    void updatePlayer_Success() throws Exception {
        long playerId = 1L;
        UpdatePlayerDetailsDTO updatedPlayerDetails = new UpdatePlayerDetailsDTO();
        doNothing().when(playerService).updatePlayer(playerId, updatedPlayerDetails);

        mockMvc.perform(put("/api/player/currentPlayerById")
                .header("X-User-PlayerId", String.valueOf(playerId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPlayerDetails)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully updated player"));

        verify(playerService).updatePlayer(playerId, updatedPlayerDetails);
    }

    @Test
    void getRecentMatches_ReturnsMatchList() throws Exception {
        long playerId = 1L;
        List<MatchResponseDTO> matches = Arrays.asList(new MatchResponseDTO(), new MatchResponseDTO());
        when(playerService.getRecentMatches(playerId)).thenReturn(matches);

        mockMvc.perform(get("/api/player/recentMatches")
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(matches)));

        verify(playerService).getRecentMatches(playerId);
    }

    @Test
    void getTop100Players_ReturnsRankingList() throws Exception {
        List<RankingDTO> topPlayers = Arrays.asList(new RankingDTO(), new RankingDTO());
        when(playerService.findTop100Players()).thenReturn(topPlayers);

        mockMvc.perform(get("/api/player/getTop100Players"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(topPlayers)));

        verify(playerService).findTop100Players();
    }

    @Test
    void uploadProfilePicture_Success() throws Exception {
        long playerId = 1L;
        MultipartFile file = mock(MultipartFile.class);
        doNothing().when(playerService).uploadProfilePicture(playerId, file);

        mockMvc.perform(multipart("/api/player/uploadProfile")
                .file("file", "dummy content".getBytes())
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile uploaded successfully"));

        verify(playerService).uploadProfilePicture(eq(playerId), any(MultipartFile.class));
    }

    @Test
    void getProfilePicture_Success() throws Exception {
        long playerId = 1L;
        byte[] photoData = "test photo".getBytes();
        when(playerService.getProfilePicture("player_" + playerId)).thenReturn(photoData);

        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(photoData));

        verify(playerService).getProfilePicture("player_" + playerId);
    }

    @Test
    void getProfilePicture_NotFound() throws Exception {
        long playerId = 1L;
        when(playerService.getProfilePicture("player_" + playerId)).thenReturn(null);

        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isNotFound());

        verify(playerService).getProfilePicture("player_" + playerId);
    }

    @Test
    void getPlayerElo_ReturnsEloScore() throws Exception {
        long playerId = 1L;
        int eloScore = 1500;
        when(playerService.getPlayerElo(playerId)).thenReturn(eloScore);

        mockMvc.perform(get("/api/player/elo/{id}", playerId))
                .andExpect(status().isOk())
                .andExpect(content().string(String.valueOf(eloScore)));

        verify(playerService).getPlayerElo(playerId);
    }

    @Test
    void updateWinLossElo_Success() throws Exception {
        WinLossUpdateDTO dto = new WinLossUpdateDTO(1L, 1550, true);
        doNothing().when(playerService).updateWinLossElo(dto);

        mockMvc.perform(put("/api/player/updateWinLossElo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(playerService).updateWinLossElo(dto);
    }


       @Test
    void getRankingForCurrentPlayer_Success() throws Exception {
        long playerId = 1L;
        int ranking = 5;
        when(playerService.getRankingForCurrentPlayer(playerId)).thenReturn(ranking);

        mockMvc.perform(get("/api/player/getRanking")
                .header("X-User-PlayerId", String.valueOf(playerId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(String.valueOf(ranking)));

        verify(playerService).getRankingForCurrentPlayer(playerId);
    }

    @Test
    void getRankingForCurrentPlayer_InvalidPlayerIdFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/player/getRanking")
                .header("X-User-PlayerId", "invalid_id"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Invalid format for numeric value: For input string: \"invalid_id\""));

        verify(playerService, never()).getRankingForCurrentPlayer(anyLong());
    }

    @Test
    void getListOfPlayerDetailsReturnsData() throws Exception {
        List<Long> playerIds = List.of(1L, 2L);
        PlayerDetailsDTO player1 = new PlayerDetailsDTO();
        player1.setId(1L);
        PlayerDetailsDTO player2 = new PlayerDetailsDTO();
        player2.setId(2L);
        
        List<PlayerDetailsDTO> playerDetails = List.of(player1, player2);
        when(playerService.getListOfPlayerDetails(playerIds)).thenReturn(playerDetails);
        
        mockMvc.perform(post("/api/player/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(playerIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));

        verify(playerService, times(1)).getListOfPlayerDetails(any());
    }

    @Test
    void getListOfPlayerDetailsHandlesEmptyList() throws Exception {
        List<Long> playerIds = List.of();
        when(playerService.getListOfPlayerDetails(any())).thenReturn(List.of());

        String jsonPayload = objectMapper.writeValueAsString(playerIds);

        mockMvc.perform(post("/api/player/list")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.length()").value(0));

        verify(playerService, times(1)).getListOfPlayerDetails(any());
    }

    @Test
    void uploadProfilePictureFailure() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());
        doThrow(new IOException("Failed to save file")).when(playerService).uploadProfilePicture(anyLong(), any(MultipartFile.class));

        mockMvc.perform(multipart("/api/player/uploadProfile")
                .file(file)
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("File upload failed"));

        verify(playerService).uploadProfilePicture(1L, file);
    }

    @Test
    void getPhotoReturnsNotFoundWhenDataIsNull() throws Exception {
        // Mock the service to return null
        when(playerService.getProfilePicture("player_1")).thenReturn(null);

        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isNotFound());

        // Verify that the service was called with the correct ID
        verify(playerService).getProfilePicture("player_1");
    }

    @Test
    void getPhotoReturnsNotFoundWhenDataIsEmpty() throws Exception {
        // Mock the service to return an empty byte array
        when(playerService.getProfilePicture("player_1")).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isNotFound());

        // Verify that the service was called with the correct ID
        verify(playerService).getProfilePicture("player_1");
    }
    @Test
    void getPhotoWithDifferentMediaTypes() throws Exception {
        // Prepare data for each media type
        byte[] jpgPhoto = "fake jpg data".getBytes();
        byte[] jpegPhoto = "fake jpeg data".getBytes();
        byte[] pngPhoto = "fake png data".getBytes();
        byte[] gifPhoto = "fake gif data".getBytes();

        // Setup service to return different data based on the filename
        when(playerService.getProfilePicture("player_1.jpg")).thenReturn(jpgPhoto);
        when(playerService.getProfilePicture("player_1.jpeg")).thenReturn(jpegPhoto);
        when(playerService.getProfilePicture("player_1.png")).thenReturn(pngPhoto);
        when(playerService.getProfilePicture("player_1.gif")).thenReturn(gifPhoto);

        // Test JPG media type
        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1.jpg"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

            // Test JPEG media type
        mockMvc.perform(get("/api/player/photo")
        .header("X-User-PlayerId", "1.jpeg"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        // Test PNG media type
        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1.png"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));

        // Test GIF media type
        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1.gif"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_GIF));

        // Verify interactions
        verify(playerService).getProfilePicture("player_1.jpg");
        verify(playerService).getProfilePicture("player_1.jpeg");
        verify(playerService).getProfilePicture("player_1.png");
        verify(playerService).getProfilePicture("player_1.gif");
    }

    @Test
    void getPhotoIOExceptionReturnsNotFound() throws Exception {
        // Mock the service to throw an IOException
        when(playerService.getProfilePicture("player_1")).thenThrow(new IOException("Failed to retrieve file"));

        mockMvc.perform(get("/api/player/photo")
                .header("X-User-PlayerId", "1"))
                .andExpect(status().isNotFound());

        // Verify that the service was called with the correct ID
        verify(playerService).getProfilePicture("player_1");
    }

    
}
