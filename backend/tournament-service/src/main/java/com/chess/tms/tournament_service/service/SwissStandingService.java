package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.SwissStandingDTO;
import com.chess.tms.tournament_service.exception.SwissBracketNotFoundException;
import com.chess.tms.tournament_service.model.SwissStanding;
import com.chess.tms.tournament_service.repository.SwissStandingRepository;

@Service
public class SwissStandingService {
    private final SwissStandingRepository swissStandingRepository;
    private final RestTemplate restTemplate;
    private final String playerServiceUrl;

    public SwissStandingService(SwissStandingRepository swissStandingRepository, 
                               RestTemplate restTemplate,
                               @Value("${players.service.url}") String playerServiceUrl) {
        this.swissStandingRepository = swissStandingRepository;
        this.restTemplate = restTemplate;
        this.playerServiceUrl = playerServiceUrl;
    }

    public List<SwissStandingDTO> getSwissStandings(Long bracketId) {
        // Fetch the list of Swiss standings for the given bracket ID
        List<SwissStanding> standings = swissStandingRepository.findAllByBracketId(bracketId);

        // Throw exception if no standings are found for the given bracket ID
        if (standings.isEmpty()) {
            throw new SwissBracketNotFoundException("No standings found for the bracket ID: " + bracketId);
        }

        // Convert each SwissStanding entity to a SwissStandingDTO
        return standings.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Helper method to convert SwissStanding to SwissStandingDTO
    private SwissStandingDTO convertToDTO(SwissStanding standing) {
        PlayerDetailsDTO playerDetails = fetchPlayerDetails(standing.getPlayerId());

        return new SwissStandingDTO(playerDetails, standing.getWins(), standing.getLosses());
    }

    private PlayerDetailsDTO fetchPlayerDetails(Long playerId) {
        ResponseEntity<PlayerDetailsDTO> response = restTemplate.getForEntity(
                playerServiceUrl + "/api/player/" + playerId, PlayerDetailsDTO.class);
        return response.getBody();
    }

}