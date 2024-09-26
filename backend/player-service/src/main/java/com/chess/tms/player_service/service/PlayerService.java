package com.chess.tms.player_service.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.chess.tms.player_service.dto.MatchDTO;
import com.chess.tms.player_service.dto.MatchResponseDTO;
import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.dto.UpdatePlayerDetailsDTO;
import com.chess.tms.player_service.exception.UserNotFoundException;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.repository.PlayerDetailsRepository;

@Service
public class PlayerService {

    @Value("${matches.service.url}")
    private String matchesServiceUrl;

    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;

    @Autowired
    private RestTemplate restTemplate;

    public PlayerDetailsDTO getPlayerDetailsById(Long id) {
        PlayerDetails playerDetails = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        PlayerDetailsDTO playerDetailsDTO = new PlayerDetailsDTO();
        playerDetailsDTO.setId(playerDetails.getId());
        playerDetailsDTO.setUserId(playerDetails.getUserId());
        playerDetailsDTO.setFirstName(playerDetails.getFirstName());
        playerDetailsDTO.setLastName(playerDetails.getLastName());
        playerDetailsDTO.setEloRating(playerDetails.getEloRating());
        playerDetailsDTO.setProfilePicture(playerDetails.getProfilePicture());
        playerDetailsDTO.setLowestElo(playerDetails.getLowestElo());
        playerDetailsDTO.setHighestElo(playerDetails.getHighestElo());
        playerDetailsDTO.setTotalMatches(playerDetails.getTotalMatches());
        playerDetailsDTO.setTotalLosses(playerDetails.getTotalLosses());
        playerDetailsDTO.setTotalWins(playerDetails.getTotalWins());
        playerDetailsDTO.setCountry(playerDetails.getCountry());
        playerDetailsDTO.setWinRate(playerDetails.getTotalWins()/playerDetails.getTotalMatches());
        return playerDetailsDTO;

    }

    public PlayerDetailsDTO getPlayerDetailsByUserId(Long id) {
        PlayerDetails playerDetails = playerDetailsRepository.findByUserId(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        PlayerDetailsDTO playerDetailsDTO = new PlayerDetailsDTO();
        playerDetailsDTO.setId(playerDetails.getId());
        playerDetailsDTO.setUserId(playerDetails.getUserId());
        playerDetailsDTO.setFirstName(playerDetails.getFirstName());
        playerDetailsDTO.setLastName(playerDetails.getLastName());
        playerDetailsDTO.setEloRating(playerDetails.getEloRating());
        playerDetailsDTO.setProfilePicture(playerDetails.getProfilePicture());
        playerDetailsDTO.setLowestElo(playerDetails.getLowestElo());
        playerDetailsDTO.setHighestElo(playerDetails.getHighestElo());
        playerDetailsDTO.setTotalMatches(playerDetails.getTotalMatches());
        playerDetailsDTO.setTotalLosses(playerDetails.getTotalLosses());
        playerDetailsDTO.setTotalWins(playerDetails.getTotalWins());
        playerDetailsDTO.setCountry(playerDetails.getCountry());
        playerDetailsDTO.setWinRate(playerDetails.getTotalWins()/playerDetails.getTotalMatches());
        return playerDetailsDTO;
    }

    public void updatePlayer(Long id, UpdatePlayerDetailsDTO updatedPlayerDetails) {
        PlayerDetails player = playerDetailsRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (updatedPlayerDetails.getCountry() != null) {
            player.setCountry(updatedPlayerDetails.getCountry());
        }
        if (updatedPlayerDetails.getFirstName() != null) {
            player.setFirstName(updatedPlayerDetails.getFirstName());
        }
        if (updatedPlayerDetails.getLastName() != null) {
            player.setLastName(updatedPlayerDetails.getLastName());
        }
        if (updatedPlayerDetails.getProfilePicture() != null) {
            player.setProfilePicture(updatedPlayerDetails.getProfilePicture());
        }

        playerDetailsRepository.save(player);
    }

    public List<MatchResponseDTO> getRecentMatches(Long playerId) {
        String url = matchesServiceUrl + "/api/matches/player/" + playerId + "/recent";
        MatchDTO[] matches = restTemplate.getForObject(url, MatchDTO[].class);
        
        return Arrays.stream(matches)
                .map(this::convertToMatchResponseDTO)
                .collect(Collectors.toList());
    }

    private MatchResponseDTO convertToMatchResponseDTO(MatchDTO matchDTO) {
        MatchResponseDTO responseDTO = new MatchResponseDTO();
        responseDTO.setTournament(matchDTO.getTournament());
        responseDTO.setRoundType(matchDTO.getRoundType());
        responseDTO.setGameType(matchDTO.getGameType());
        responseDTO.setDate(matchDTO.getDate());

        // Fetch winner and loser details
        PlayerDetails winner = fetchPlayerDetails(matchDTO.getWinnerId()) .orElseThrow(() -> new UserNotFoundException("User not found"));;
        PlayerDetails loser = fetchPlayerDetails(matchDTO.getLoserId()) .orElseThrow(() -> new UserNotFoundException("User not found"));;

        responseDTO.setWinner(winner);
        responseDTO.setLoser(loser);

        return responseDTO;
    }

    private Optional<PlayerDetails> fetchPlayerDetails(Long playerId) {
        if (playerId == null) {
            return null;
        }
        return playerDetailsRepository.findById(playerId);
    }
}
