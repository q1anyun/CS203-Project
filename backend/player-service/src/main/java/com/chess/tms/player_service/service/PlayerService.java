package com.chess.tms.player_service.service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Limit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import com.chess.tms.player_service.dto.MatchDTO;
import com.chess.tms.player_service.dto.MatchResponseDTO;
import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.RankingDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.dto.WinLossUpdateDTO;
import com.chess.tms.player_service.exception.UserNotFoundException;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.repository.PlayerDetailsRepository;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class PlayerService {

    @Value("${s3.upload.service.url}")
    private String s3UploadServiceUrl;

    @Value("${matches.service.url}")
    private String matchesServiceUrl;

    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;

    @Autowired
    private RestTemplate restTemplate;

    // Fetch all player details
    public List<PlayerDetailsDTO> getAllPlayers() {
        List<PlayerDetails> players = playerDetailsRepository.findAll();
        return players.stream()
                .map(this::convertToPlayerDetailsDTO)
                .collect(Collectors.toList());
    }

    // Fetch player details by player ID
    public PlayerDetailsDTO getPlayerDetailsById(Long id) {

        return playerDetailsRepository.findById(id)
                .map(this::convertToPlayerDetailsDTO)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));
    }

    // Fetch multiple player details in a batch
    public List<PlayerDetailsDTO> getListOfPlayerDetails(List<Long> playerIds) {
        List<PlayerDetails> players = playerDetailsRepository.findByIdIn(playerIds);
        return players.stream()
                .map(this::convertToPlayerDetailsDTO)
                .collect(Collectors.toList());
    }

    // Convert PlayerDetails to PlayerDetailsDTO
    private PlayerDetailsDTO convertToPlayerDetailsDTO(PlayerDetails playerDetails) {
        PlayerDetailsDTO dto = new PlayerDetailsDTO();
        dto.setId(playerDetails.getId());
        dto.setUserId(playerDetails.getUserId());
        dto.setFirstName(playerDetails.getFirstName());
        dto.setLastName(playerDetails.getLastName());
        dto.setEloRating(playerDetails.getEloRating());
        dto.setProfilePicture(playerDetails.getProfilePicture());
        dto.setTotalMatches(playerDetails.getTotalMatches());
        dto.setTotalLosses(playerDetails.getTotalLosses());
        dto.setTotalWins(playerDetails.getTotalWins());
        dto.setCountry(playerDetails.getCountry());
        return dto;
    }

    private RankingDTO convertToRankingDTO(PlayerDetails playerDetails) {
        RankingDTO dto = new RankingDTO();
        dto.setUserId(playerDetails.getUserId());
        dto.setFirstName(playerDetails.getFirstName());
        dto.setLastName(playerDetails.getLastName());
        dto.setEloRating(playerDetails.getEloRating());
        dto.setProfilePicture(playerDetails.getProfilePicture());

        return dto;
    }

    // Update player details
    public void updatePlayer(Long id, UpdatePlayerDetailsDTO updatedPlayerDetails) {
        PlayerDetails player = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));

        Optional.ofNullable(updatedPlayerDetails.getCountry()).ifPresent(player::setCountry);
        Optional.ofNullable(updatedPlayerDetails.getFirstName()).ifPresent(player::setFirstName);
        Optional.ofNullable(updatedPlayerDetails.getLastName()).ifPresent(player::setLastName);
        Optional.ofNullable(updatedPlayerDetails.getProfilePicture()).ifPresent(player::setProfilePicture);

        playerDetailsRepository.save(player);
    }

    // Fetch recent matches of a player from the matches service
    public List<MatchResponseDTO> getRecentMatches(Long playerId) {
        String url = matchesServiceUrl + "/api/matches/player/" + playerId + "/recent";

        try {
            MatchDTO[] matches = restTemplate.getForObject(url, MatchDTO[].class);
            return Arrays.stream(matches)
                    .map(this::convertToMatchResponseDTO)
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to fetch recent matches for playerId: " + playerId, e);
        }
    }

    // Convert MatchDTO to MatchResponseDTO
    private MatchResponseDTO convertToMatchResponseDTO(MatchDTO matchDTO) {
        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setTournament(matchDTO.getTournament());
        responseDTO.setRoundType(matchDTO.getRoundType());
        responseDTO.setGameType(matchDTO.getGameType());
        responseDTO.setDate(matchDTO.getDate());

        // Fetch winner and loser details

        responseDTO.setWinner(fetchPlayerDetails(matchDTO.getWinnerId())
                .orElseThrow(() -> new UserNotFoundException("Winner not found")));
        responseDTO.setLoser(fetchPlayerDetails(matchDTO.getLoserId())
                .orElseThrow(() -> new UserNotFoundException("Loser not found")));

        return responseDTO;
    }

    // Fetch player details by ID
    private Optional<PlayerDetails> fetchPlayerDetails(Long playerId) {
        if (playerId == null) {

            return Optional.empty();
        }
        return playerDetailsRepository.findById(playerId);
    }

    public List<RankingDTO> findTop100Players() {
        List<PlayerDetails> top100 = playerDetailsRepository.findByOrderByEloRatingDesc(Limit.of(100));

        Iterator<PlayerDetails> iterator = top100.iterator();

        List<RankingDTO> dtoList = new ArrayList<>();

        while (iterator.hasNext()) {
            PlayerDetails player = iterator.next();
            RankingDTO dto = convertToRankingDTO(player);
            dtoList.add(dto);
        }

        return dtoList;
    }

    // Update player elo rating
    public void updateWinLossElo(WinLossUpdateDTO dto) {
        PlayerDetails player = playerDetailsRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new UserNotFoundException("Player with id " + dto.getPlayerId() + " not found"));

        player.setTotalMatches(player.getTotalMatches() + 1);

        player.setEloRating(dto.getNewElo());

        if (dto.isWinner()) {
            player.setTotalWins(player.getTotalWins() + 1);
        } else {
            player.setTotalLosses(player.getTotalLosses() + 1);
        }

        if (player.getHighestElo() == null || dto.getNewElo() > player.getHighestElo()) {
            player.setHighestElo(dto.getNewElo());
        }

        playerDetailsRepository.save(player);
    }

    public Integer getPlayerElo(long id) {
        PlayerDetails player = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));
        return player.getEloRating();
    }

    // Method to upload a player's profile picture using RestTemplate
    public void uploadProfilePicture(Long playerId, MultipartFile file) throws IOException {
        String url =  s3UploadServiceUrl + "/api/s3/upload"; // Adjust based on your actual endpoint

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("X-User-PlayerId", String.valueOf(playerId)); // Add custom header for player ID

        // Create a multi-value map for the request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Directly use the MultipartFile
        body.add("filename", "player_" + playerId); // Include the filename as a separate part

        // Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Check response status
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload profile picture: " + response.getBody());
        }

    }

   
    // Method to retrieve a player's profile picture by filename
    public byte[] getProfilePicture(String filename) throws IOException {
        String url = s3UploadServiceUrl + "/api/s3/find/" + filename; // Construct the find URL

        // Make the request to retrieve the file
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

        // Check response status
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to retrieve profile picture: " + response.getBody());
        }

        // Return the byte array
        return response.getBody();
    }

    public Integer getRankingForCurrentPlayer(Long playerId) {
        PlayerDetails player = playerDetailsRepository.findById(playerId)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + playerId + " not found"));

        return player.getEloRating();
    }

    // public Integer updateWinLoss(WinLossUpdateDTO dto) {
    // PlayerDetails player = playerDetailsRepository.findById(dto.getPlayerId())
    // .orElseThrow(() -> new UserNotFoundException("Player with id " +
    // dto.getPlayerId() + " not found"));

    // int toReturn = player.getTotalLosses() - 1;
    // if(dto.isWinner()) {
    // toReturn = player.getTotalWins() + 1;
    // player.setTotalWins(toReturn);
    // } else {
    // player.setTotalLosses(toReturn);
    // }

    // playerDetailsRepository.save(player);

    // return toReturn;
    // }

}