package com.chess.tms.player_service.unit.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.chess.tms.player_service.dto.*;
import com.chess.tms.player_service.exception.UserNotFoundException;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.repository.PlayerDetailsRepository;
import com.chess.tms.player_service.service.PlayerService;

@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private PlayerDetailsRepository playerDetailsRepository;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MultipartFile mockMultipartFile;

    @Mock
    private Resource mockResource;

    @InjectMocks
    private PlayerService playerService;

    private PlayerDetails samplePlayer;
    private PlayerDetails samplePlayer2;
    private MatchDTO sampleMatchDTO;
    private TournamentDTO sampleTournament;
    private RoundTypeDTO sampleRoundType;
    private GameTypeDTO sampleGameType;

    @BeforeEach
    void setUp() {
        // Setup sample player
        samplePlayer = new PlayerDetails();
        samplePlayer.setId(1L);
        samplePlayer.setUserId(1L);
        samplePlayer.setFirstName("Magnus");
        samplePlayer.setLastName("Carlsen");
        samplePlayer.setEloRating(2850);
        samplePlayer.setTotalMatches(100);
        samplePlayer.setTotalWins(70);
        samplePlayer.setTotalLosses(30);
        samplePlayer.setCountry("Norway");
        samplePlayer.setHighestElo(2882);

        // Setup sample player
        samplePlayer2 = new PlayerDetails();
        samplePlayer2.setId(2L);
        samplePlayer2.setUserId(2L);
        samplePlayer2.setFirstName("Magnus");
        samplePlayer2.setLastName("Carlsen");
        samplePlayer2.setEloRating(2850);
        samplePlayer2.setTotalMatches(100);
        samplePlayer2.setTotalWins(70);
        samplePlayer2.setTotalLosses(30);
        samplePlayer2.setCountry("Norway");
        samplePlayer2.setHighestElo(2882);

        // Setup sample tournament
        sampleTournament = new TournamentDTO();
        sampleTournament.setTournamentId(1L);
        sampleTournament.setName("World Chess Championship 2024");

        // Setup sample round type
        sampleRoundType = new RoundTypeDTO();
        sampleRoundType.setId(1L);
        sampleRoundType.setRoundName("Final");
        sampleRoundType.setNumberOfPlayers(2);

        // Setup sample game type
        sampleGameType = new GameTypeDTO();
        sampleGameType.setId(1L);
        sampleGameType.setName("Classical");
        sampleGameType.setTimeControlMinutes(5);

        // Setup sample match
        sampleMatchDTO = new MatchDTO();
        sampleMatchDTO.setWinnerId(1L);
        sampleMatchDTO.setLoserId(2L);
        sampleMatchDTO.setTournament(sampleTournament);
        sampleMatchDTO.setGameType(sampleGameType);
        sampleMatchDTO.setRoundType(sampleRoundType);
        sampleMatchDTO.setDate(LocalDateTime.now());
    }

    @Test
    void getAllPlayers_ShouldReturnListOfPlayerDTOs() {
        when(playerDetailsRepository.findAll()).thenReturn(Arrays.asList(samplePlayer));

        List<PlayerDetailsDTO> result = playerService.getAllPlayers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(samplePlayer.getFirstName(), result.get(0).getFirstName());
        verify(playerDetailsRepository).findAll();
    }

    @Test
    void getPlayerDetailsById_WhenPlayerExists_ShouldReturnPlayerDTO() {
        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        PlayerDetailsDTO result = playerService.getPlayerDetailsById(1L);

        assertNotNull(result);
        assertEquals(samplePlayer.getFirstName(), result.getFirstName());
        verify(playerDetailsRepository).findById(1L);
    }

    @Test
    void getPlayerDetailsById_WhenPlayerDoesNotExist_ShouldThrowException() {
        when(playerDetailsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            playerService.getPlayerDetailsById(999L);
        });
    }

    @Test
    void getRecentMatches_ShouldReturnMatchResponseDTOs() {
        when(restTemplate.getForObject(anyString(), eq(MatchDTO[].class)))
                .thenReturn(new MatchDTO[] { sampleMatchDTO });
        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(playerDetailsRepository.findById(2L)).thenReturn(Optional.of(samplePlayer2));

        List<MatchResponseDTO> result = playerService.getRecentMatches(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleMatchDTO.getTournament(), result.get(0).getTournament());
    }

    @Test
    void getRecentMatches_WhenLoserIdIsNull_ShouldThrowException() {
        sampleMatchDTO.setLoserId(null);
        when(restTemplate.getForObject(anyString(), eq(MatchDTO[].class)))
                .thenReturn(new MatchDTO[] { sampleMatchDTO });

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        assertThrows(UserNotFoundException.class, () -> {
            playerService.getRecentMatches(1L);
        });
    }

    @Test
    void getRecentMatches_WhenHttpClientError_ShouldThrowException() {
        when(restTemplate.getForObject(anyString(), eq(MatchDTO[].class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            playerService.getRecentMatches(1L);
        });

        assertEquals("Failed to fetch recent matches for playerId: " + 1L, exception.getMessage());
        assertTrue(exception.getCause() instanceof RuntimeException);
    }

    @Test
    void updateWinLossElo_ShouldUpdatePlayerStats() {
        WinLossUpdateDTO updateDTO = new WinLossUpdateDTO();
        updateDTO.setPlayerId(1L);
        updateDTO.setWinner(true);
        updateDTO.setNewElo(2860);

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(playerDetailsRepository.save(any(PlayerDetails.class))).thenReturn(samplePlayer);

        playerService.updateWinLossElo(updateDTO);

        verify(playerDetailsRepository).save(argThat(player -> player.getTotalMatches() == 101 &&
                player.getTotalWins() == 71 &&
                player.getEloRating() == 2860 &&
                player.getHighestElo() == 2882));
    }

    @Test
    void uploadProfilePicture_ShouldCallS3Service_Success() throws IOException {
        // Mock MultipartFile behavior
        when(mockMultipartFile.getResource()).thenReturn(mockResource);

        // Mock successful response from S3 service
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(mockResponse);

        assertDoesNotThrow(() -> playerService.uploadProfilePicture(1L, mockMultipartFile));

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void uploadProfilePicture_WhenUploadFails_ShouldThrowException() throws IOException {
        // Mock MultipartFile behavior
        when(mockMultipartFile.getResource()).thenReturn(mockResource);

        // Mock failed response from S3 service
        ResponseEntity<String> mockResponse = new ResponseEntity<>("Upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class))).thenReturn(mockResponse);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> playerService.uploadProfilePicture(1L, mockMultipartFile));

        assertEquals("Failed to upload profile picture: Upload failed", exception.getMessage());

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void getProfilePicture_ShouldReturnBytes() throws IOException {
        byte[] mockImageData = "mock image data".getBytes();
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(mockImageData, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class))).thenReturn(mockResponse);

        byte[] result = playerService.getProfilePicture("test.jpg");

        assertNotNull(result);
        assertArrayEquals(mockImageData, result);
    }

    @Test
    void getProfilePicture_ShouldThrowException_WhenFileNotFound() {
        // Mock data
        String filename = "nonexistent_file";

        // Mock 404 response
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        when(restTemplate.exchange(
                contains("/api/s3/find/" + filename),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class))).thenReturn(mockResponse);

        // Test
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> playerService.getProfilePicture(filename));

        // Verify
        assertEquals("Failed to retrieve profile picture: null", exception.getMessage());
        verify(restTemplate).exchange(
                contains("/api/s3/find/" + filename),
                eq(HttpMethod.GET),
                isNull(),
                eq(byte[].class));
    }

    @Test
    void findTop100Players_ShouldReturnRankingDTOs() {
        when(playerDetailsRepository.findByOrderByEloRatingDesc(any(Limit.class)))
                .thenReturn(Arrays.asList(samplePlayer));

        List<RankingDTO> result = playerService.findTop100Players();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(samplePlayer.getEloRating(), result.get(0).getEloRating());
        verify(playerDetailsRepository).findByOrderByEloRatingDesc(Limit.of(100));
    }

    @Test
    void updatePlayer_ShouldUpdateOnlyProvidedFields() {
        UpdatePlayerDetailsDTO updateDTO = new UpdatePlayerDetailsDTO();
        updateDTO.setFirstName("New Name");
        // Other fields are null

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));
        when(playerDetailsRepository.save(any(PlayerDetails.class))).thenReturn(samplePlayer);

        playerService.updatePlayer(1L, updateDTO);

        verify(playerDetailsRepository).save(argThat(player -> player.getFirstName().equals("New Name") &&
                player.getLastName().equals(samplePlayer.getLastName()) && // Should remain unchanged
                player.getCountry().equals(samplePlayer.getCountry()) // Should remain unchanged
        ));
    }

    @Test
    public void getListOfPlayerDetails_WhenListIsValid_ReturnsPlayerDetailsDTOs() {
        // Arrange
        List<Long> ids = Arrays.asList(1L, 2L);
        List<PlayerDetails> players = Arrays.asList(samplePlayer, samplePlayer2);

        when(playerDetailsRepository.findByIdIn(ids)).thenReturn(players);

        // Act
        List<PlayerDetailsDTO> result = playerService.getListOfPlayerDetails(ids);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Magnus", result.get(0).getFirstName());
        assertEquals("Magnus", result.get(1).getFirstName());
    }

    @Test
    public void getListOfPlayerDetails_WhenListIsEmpty_ReturnsEmptyList() {
        // Arrange
        List<Long> ids = Arrays.asList();

        when(playerDetailsRepository.findByIdIn(ids)).thenReturn(Arrays.asList());

        // Act
        List<PlayerDetailsDTO> result = playerService.getListOfPlayerDetails(ids);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPlayerElo_WhenPlayerExists_ReturnsEloRating() {

        samplePlayer.setEloRating(1500);
        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        // Act
        int eloRating = playerService.getPlayerElo(1L);

        // Assert
        assertEquals(1500, eloRating);
    }

    @Test
    void updateWinLossElo_WhenPlayerWins_IncrementsWinsAndMatches() {
        // Arrange
        WinLossUpdateDTO dto = new WinLossUpdateDTO();
        dto.setPlayerId(1L);
        dto.setNewElo(2900);
        dto.setWinner(true);

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        // Act
        playerService.updateWinLossElo(dto);

        // Assert
        assertEquals(101, samplePlayer.getTotalMatches());
        assertEquals(71, samplePlayer.getTotalWins());
        assertEquals(2900, samplePlayer.getEloRating());
        assertEquals(2900, samplePlayer.getHighestElo());
        verify(playerDetailsRepository).save(samplePlayer);
    }

    @Test
    void updateWinLossElo_WhenPlayerLoses_IncrementsLossesAndMatches() {
        // Arrange
        WinLossUpdateDTO dto = new WinLossUpdateDTO();
        dto.setPlayerId(1L);
        dto.setNewElo(2800);
        dto.setWinner(false);

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        // Act
        playerService.updateWinLossElo(dto);

        // Assert
        assertEquals(101, samplePlayer.getTotalMatches());
        assertEquals(31, samplePlayer.getTotalLosses());
        assertEquals(2800, samplePlayer.getEloRating());
        verify(playerDetailsRepository).save(samplePlayer);
    }

    @Test
    void updateWinLossElo_WhenPlayerNotFound_ThrowsException() {
        // Arrange
        WinLossUpdateDTO dto = new WinLossUpdateDTO();
        dto.setPlayerId(99L); // Assuming ID 99 does not exist
        dto.setNewElo(2800);
        dto.setWinner(true);

        when(playerDetailsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> playerService.updateWinLossElo(dto));
    }

    @Test
    void updateWinLossElo_WhenHighestEloIsNull_ShouldUpdateHighestElo() {
        // Arrange
        WinLossUpdateDTO dto = new WinLossUpdateDTO();
        dto.setPlayerId(1L);
        dto.setNewElo(2850);
        dto.setWinner(true);

        samplePlayer.setHighestElo(null);

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        // Act
        playerService.updateWinLossElo(dto);

        // Assert
        assertEquals(dto.getNewElo(), samplePlayer.getHighestElo(), "Highest Elo should be updated to new Elo");
        verify(playerDetailsRepository).save(samplePlayer);
    }

    @Test
    void getRankingForCurrentPlayer_ReturnsEloRating_WhenPlayerExists() {
        // Arrange

        when(playerDetailsRepository.findById(1L)).thenReturn(Optional.of(samplePlayer));

        // Act
        int eloRating = playerService.getRankingForCurrentPlayer(1L);

        // Assert
        assertEquals(2850, eloRating);
    }

    @Test
    void getRankingForCurrentPlayer_ThrowsUserNotFoundException_WhenPlayerNotFound() {
        // Arrange
        long playerId = 99L; // Assume no player exists with this ID
        when(playerDetailsRepository.findById(playerId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            playerService.getRankingForCurrentPlayer(playerId);
        });
    }

}