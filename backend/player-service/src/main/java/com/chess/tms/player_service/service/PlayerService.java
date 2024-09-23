package com.chess.tms.player_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.player_service.dto.PlayerDetailsDTO;
import com.chess.tms.player_service.exception.UserNotFoundException;
import com.chess.tms.player_service.model.PlayerDetails;
import com.chess.tms.player_service.repository.PlayerDetailsRepository;

@Service
public class PlayerService {

    @Autowired
    private PlayerDetailsRepository playerDetailsRepository;

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
        return playerDetailsDTO;
    }
}
