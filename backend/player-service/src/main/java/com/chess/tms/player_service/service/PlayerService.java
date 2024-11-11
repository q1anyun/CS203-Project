package com.chess.tms.player_service.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    /**
     * Fetches all player details from the database.
     * 
     * This method retrieves a list of all players from the player details
     * repository and converts each player entity into a Data Transfer Object (DTO)
     * for easier
     * handling in other layers of the application.
     * 
     * @return A list of PlayerDetailsDTO objects containing the details of all
     *         players.
     */
    public List<PlayerDetailsDTO> getAllPlayers() {
        // Fetch all player details from the repository
        List<PlayerDetails> players = playerDetailsRepository.findAll();

        // Convert each player entity to a DTO and return as a list
        return players.stream()
                .map(this::convertToPlayerDetailsDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetches the details of a specific player by their ID.
     * 
     * This method retrieves the player details from the repository using the given
     * player ID. If the player exists, it converts the player entity to a DTO.
     * If the player is not found, it throws a custom exception indicating that
     * the player with the provided ID does not exist.
     * 
     * @param id The ID of the player whose details are to be retrieved.
     * @return A PlayerDetailsDTO containing the details of the player.
     * @throws UserNotFoundException If no player is found with the given ID.
     */
    public PlayerDetailsDTO getPlayerDetailsById(Long id) {
        return playerDetailsRepository.findById(id)
                .map(this::convertToPlayerDetailsDTO)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));
    }

    /**
     * Fetches player details by their ID.
     * 
     * This method checks if the provided player ID is null. If it is, it returns an
     * empty Optional. Otherwise, it attempts to fetch the player details from the
     * repository using the provided player ID.
     * 
     * @param playerId The ID of the player whose details are to be fetched.
     * @return An Optional containing the PlayerDetails if found, or an empty
     *         Optional if not found or ID is null.
     */
    private Optional<PlayerDetails> fetchPlayerDetails(Long playerId) {
        if (playerId == null) {

            return Optional.empty();
        }
        return playerDetailsRepository.findById(playerId);
    }

    /**
     * Fetches details of multiple players in a batch.
     * 
     * This method retrieves player details for the list of player IDs provided. It
     * uses the repository method `findByIdIn()` to fetch all players that match the
     * given player IDs. It then converts the list of `PlayerDetails` entities to
     * `PlayerDetailsDTO` objects and returns the list of DTOs.
     * 
     * @param playerIds A list of player IDs for which details are to be fetched.
     * @return A list of PlayerDetailsDTO containing the details of the players.
     */
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
        dto.setHighestElo(playerDetails.getHighestElo());
        return dto;
    }

    // Convert PlayerDetails to RankingDTO
    private RankingDTO convertToRankingDTO(PlayerDetails playerDetails) {
        RankingDTO dto = new RankingDTO();
        dto.setPlayerId(playerDetails.getId());
        dto.setFirstName(playerDetails.getFirstName());
        dto.setLastName(playerDetails.getLastName());
        dto.setEloRating(playerDetails.getEloRating());
        dto.setProfilePicture(playerDetails.getProfilePicture());
        return dto;
    }

    /**
     * Updates the details of an existing player.
     * 
     * This method retrieves the player with the specified ID, updates the player's
     * details (country, first name, last name, profile picture) using the provided
     * `UpdatePlayerDetailsDTO`, and saves the updated player back to the
     * repository.
     * 
     * @param id                   The ID of the player whose details need to be
     *                             updated.
     * @param updatedPlayerDetails The DTO containing the updated player details.
     * @return A confirmation message indicating successful update.
     * @throws UserNotFoundException If no player is found with the specified ID.
     */
    public String updatePlayer(Long id, UpdatePlayerDetailsDTO updatedPlayerDetails) {
        PlayerDetails player = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));

        Optional.ofNullable(updatedPlayerDetails.getCountry()).ifPresent(player::setCountry);
        Optional.ofNullable(updatedPlayerDetails.getFirstName()).ifPresent(player::setFirstName);
        Optional.ofNullable(updatedPlayerDetails.getLastName()).ifPresent(player::setLastName);
        Optional.ofNullable(updatedPlayerDetails.getProfilePicture()).ifPresent(player::setProfilePicture);

        playerDetailsRepository.save(player);

        return "Successfully updated player with ID " + id;
    }

    /**
     * Fetches the recent matches for a given player by interacting with the matches
     * service.
     * 
     * This method constructs the URL to retrieve recent matches for a player from
     * the
     * external matches service. It makes an HTTP GET request to fetch the match
     * data and
     * then maps the response into a list of `MatchResponseDTO` objects.
     * 
     * @param playerId The ID of the player whose recent matches need to be fetched.
     * @return A list of `MatchResponseDTO` objects containing details of the
     *         player's recent matches.
     * @throws RuntimeException If the request to the matches service fails or the
     *                          response is invalid.
     */
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

    /**
     * Retrieves the top 100 players based on their Elo rating in descending order.
     * 
     * This method queries the player details repository to fetch the top 100
     * players, sorted by their Elo rating in descending order. It then converts
     * each player
     * to a `RankingDTO` and returns the list of `RankingDTO` objects.
     * 
     * @return A list of `RankingDTO` objects representing the top 100 players based
     *         on their Elo rating.
     */
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

    /**
     * Updates the Elo rating of a player based on a win or loss.
     * 
     * This method takes in a `WinLossUpdateDTO` that contains the player's ID,
     * the new Elo rating, and whether the player won or lost. It updates the
     * player's
     * Elo rating, total matches, total wins/losses, and highest Elo rating
     * accordingly.
     * 
     * @param dto The DTO containing the player ID, new Elo rating, and match result
     *            (win/loss).
     * @throws UserNotFoundException If the player with the given ID is not found in
     *                               the repository.
     */
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

    /**
     * Retrieves the Elo rating of a player by their ID.
     * 
     * This method fetches the player details from the repository using the player
     * ID.
     * If the player is found, their current Elo rating is returned.
     * If the player is not found, a `UserNotFoundException` is thrown.
     * 
     * @param id The ID of the player whose Elo rating is to be retrieved.
     * @return The Elo rating of the player.
     * @throws UserNotFoundException If the player with the given ID is not found in
     *                               the repository.
     */
    public Integer getPlayerElo(long id) {
        PlayerDetails player = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + id + " not found"));
        return player.getEloRating();
    }

    /**
     * Uploads a player's profile picture to an external service using RestTemplate.
     * 
     * This method constructs an HTTP POST request to upload a profile picture for
     * the player.
     * The file is sent as a multipart form-data request with the player ID included
     * as a custom header.
     * If the upload is successful, a success message is returned. If it fails, an
     * exception is thrown.
     * 
     * @param playerId The ID of the player whose profile picture is being uploaded.
     * @param file     The profile picture file to be uploaded.
     * @return A message indicating the success of the upload operation.
     * @throws IOException      If there is an error during the file upload process
     *                          or communication with the external service.
     * @throws RuntimeException If the upload fails (non-2xx response).
     */
    public String uploadProfilePicture(Long playerId, MultipartFile file) throws IOException {
        String url = s3UploadServiceUrl + "/api/s3/upload"; // Adjust based on your actual endpoint

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

        return "Profile uploaded successfully";
    }

    /**
     * Retrieves a player's profile picture by filename from an external service.
     * 
     * This method sends an HTTP GET request to fetch a player's profile picture
     * from the
     * external service using the provided filename. The profile picture is returned
     * as a byte array.
     * If the retrieval fails, an exception is thrown.
     * 
     * @param filename The filename of the profile picture to retrieve.
     * @return A byte array containing the profile picture data.
     * @throws IOException      If there is an error during the retrieval process or
     *                          communication with the external service.
     * @throws RuntimeException If the response status is not successful (non-2xx
     *                          status code).
     */
    public byte[] getProfilePicture(String filename) throws IOException {
        String url = s3UploadServiceUrl + "/api/s3/find/" + filename; // Construct the find URL

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to retrieve profile picture: " + response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            // Handle the case where the file is not found (404)
            throw new FileNotFoundException("Profile picture not found for filename: " + filename);
        } catch (HttpClientErrorException e) {
            // Handle other HTTP errors (e.g., 403 Forbidden)
            throw new RuntimeException("Error retrieving profile picture: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the Elo rating (ranking) for the current player.
     * 
     * This method fetches the player's details from the database using the provided
     * player ID,
     * and returns the player's Elo rating, which is used as their ranking.
     * If the player is not found, an exception is thrown.
     * 
     * @param playerId The ID of the player whose ranking (Elo rating) is to be
     *                 retrieved.
     * @return The Elo rating of the player, representing their ranking.
     * @throws UserNotFoundException If the player with the provided ID is not found
     *                               in the database.
     */
    public Integer getRankingForCurrentPlayer(Long playerId) {
        PlayerDetails player = playerDetailsRepository.findById(playerId)
                .orElseThrow(() -> new UserNotFoundException("Player with id " + playerId + " not found"));

        return player.getEloRating();
    }

}