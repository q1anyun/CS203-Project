package com.chess.tms.player_analytics_service.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.player_analytics_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_analytics_service.dto.PlayerRegistrationDTO;
import com.chess.tms.player_analytics_service.model.PlayerDetails;
import com.chess.tms.player_analytics_service.repository.PlayerDetailsRepository;

@Service
public class PlayerAnalyticsService {

    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;

    public static final int DEFAULT_ELO_RATING = 500;

    public Optional<PlayerDetailsDTO> getPlayerDetailsById(Long id) {
        Optional<PlayerDetails> playerDetails = playerDetailsRepository.findById(id);

        if (playerDetails.isPresent()) {
            PlayerDetailsDTO playerDetailsDTO = new PlayerDetailsDTO();
            playerDetailsDTO.setId(playerDetails.get().getId());
            playerDetailsDTO.setUserId(playerDetails.get().getUserId());
            playerDetailsDTO.setFirstName(playerDetails.get().getFirstName());
            playerDetailsDTO.setLastName(playerDetails.get().getLastName());
            playerDetailsDTO.setEloRating(playerDetails.get().getEloRating());
            playerDetailsDTO.setProfilePicture(playerDetails.get().getProfilePicture());
            return Optional.of(playerDetailsDTO);
        }
        return Optional.empty();
    }

    public PlayerDetails getPlayerDetailsByUserId(Long id) {
        return playerDetailsRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public String createPlayer(PlayerRegistrationDTO playerRegistrationDTO) {
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setUserId(playerRegistrationDTO.getUserId());
        playerDetails.setFirstName(playerRegistrationDTO.getFirstName());
        playerDetails.setLastName(playerRegistrationDTO.getLastName());
        playerDetails.setEloRating(DEFAULT_ELO_RATING);
        playerDetails.setProfilePicture(playerRegistrationDTO.getProfilePicture());
        playerDetailsRepository.save(playerDetails);
        return "Player created successfully";
    }

}
