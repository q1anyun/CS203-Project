package com.chess.tms.elo_service.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.dto.WinLossUpdateDTO;
import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.exception.InvalidReasonException;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;
import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;



@Service
public class EloService {

    @Value("${players.service.url}")
    private String playersServiceUrl;

    @Autowired
    private EloRepository eloRepository;
    
    @Autowired
    private RestTemplate restTemplate;

    //Constructor injection 
    public EloService(EloRepository eloRepository, RestTemplate restTemplate) {
        this.eloRepository = eloRepository;
        this.restTemplate = restTemplate;
    }


    public List<EloResponseDTO> findAllByEloHistory() {
        List<EloHistory> eloHistorylist = eloRepository.findAll();
        List<EloResponseDTO> eloResponseList = convertEntriesToResponseDTOs(eloHistorylist);

        return eloResponseList;
    }

    /**
     * This function returns all EloHistories of a given player 
     * @param playerId
     * @return a list of EloResponseDTOs representing EloHistories of given player
     */
    public List<EloResponseDTO> findEloHistoryByPlayerId(long playerId) {
        Optional<List<EloHistory>> eloHistoryList = eloRepository.findByPlayerId(playerId);

        if(eloHistoryList.isEmpty()) {
            throw new PlayerHistoryNotFoundException("Player with player id " + playerId + " has no history.");
        }

        List<EloResponseDTO> eloResponseList = convertEntriesToResponseDTOs(eloHistoryList.get());
        return eloResponseList;
    }

    /**
     * This fuction deletes all EloHistories of a given player
     * @param playerId
     * @return list of EloResponseDTOs representing deleted EloHistories
     */

    @Transactional(dontRollbackOn = PlayerHistoryNotFoundException.class)
    public List<EloResponseDTO> deleteByPlayerId(long playerId) {
        System.out.println("Running deleteByPlayerId");
        Optional<List<EloHistory>> eloHistoryList = eloRepository.findByPlayerId(playerId);
        System.out.println("Optional<List<EloHistory>> eloHistoryList = " + eloHistoryList);
        if (eloHistoryList.isEmpty()) {
            throw new PlayerHistoryNotFoundException("Player with player id " + playerId + " has no history");
        }
        List<EloResponseDTO> deletedHistoryList = convertEntriesToResponseDTOs(eloHistoryList.get());

        eloRepository.deleteByPlayerId(playerId);
        return deletedHistoryList;
    }

    /**
     * This function is called after a match has been completed to save a new EloHistory containing the 
     * new elo of the player.
     * @param oldElo
     * @param dto
     * @param time
     * @return EloResponseDTO representing the saved EloHistory
     */
    public EloResponseDTO saveEloHistory(int oldElo, EloHistoryRequestDTO eloHistoryRequestDTO, LocalDateTime time) {
        EloHistory newEloHistory = new EloHistory();

        newEloHistory.setOldElo(oldElo);
        newEloHistory.setNewElo(eloHistoryRequestDTO.getNewElo());
        newEloHistory.setPlayerId(eloHistoryRequestDTO.getPlayerId());
        newEloHistory.setChangeReason(eloHistoryRequestDTO.getChangeReason());
        newEloHistory.setCreatedAt(time);
        eloRepository.save(newEloHistory);

        return convertEntryToResponseDTO(newEloHistory);
    }
    
    /**
     * Find all EloHistories corresponding to given playerId and reason
     * @param playerId
     * @param reasonString
     * @return  list of EloResponseDTOs representing corresponding EloHistories
     */
    public List<EloResponseDTO> findByPlayerIdAndChangeReason(long playerId, String reasonString) {
        Reason reason = Reason.WIN;
        if (reasonString.equals("win")) {
            reason = Reason.WIN;
        } else if (reasonString.equals("loss")) {
            reason = Reason.LOSS;
        } else {
            throw new InvalidReasonException("Reason: " + reasonString + " is not valid. Valid reasons: win, loss, draw");
        }

        List<EloHistory> list = eloRepository.findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(playerId, reason);
        List<EloResponseDTO> responses = convertEntriesToResponseDTOs(list);
        return responses;
    }

     /**
     * This function calculates the new elo for a player based on the current elo of the player and the elo change.
     * The elochange is calculated based on the probability of winning against the opponent, as well as the elo difference
     * between the two players.
     * The max eloChange between the two players is set at 32.
     * @param winnerElo The current elo of the winner.
     * @param loserElo The current elo of the loser.
     * @return An array containing the new elo for the winner and the loser.
     */
    public int[] calculateEloChange(int winnerElo, int loserElo) {
        int maxEloChange = 32;
        int[] changedElo = new int[2];
        
        // Algorithm to calculate win probability for both players
        double winnerWinProb = 1.0 / (1.0 + (Math.pow(10, (loserElo - winnerElo) / 400)));
        double loserWinProb = 1.0 / (1.0 + (Math.pow(10, (winnerElo - loserElo) / 400)));

        // Calculate new elo based on win probability
        changedElo[0] = (int) (winnerElo + (1.0 - winnerWinProb) * maxEloChange);
        changedElo[1] = (int) (loserElo + (0.0 - loserWinProb) * maxEloChange);

        // Handles edge case where the changedElo is the same as the currentElo of player
        if (changedElo[0] == winnerElo) changedElo[0] += 1;
        if (changedElo[1] == loserElo) changedElo[1] -= 1;

        return changedElo;
    }

    /**
     * This function updates the elo of winner and loser after a completed match in player service.
     * @param matchEloRequestDTO
     * @return String: "Player's Elo updated successfully"
     */ 
    public String updatePlayersEloAfterCompletedMatch(MatchEloRequestDTO matchEloRequestDTO) {
        long winnerId = matchEloRequestDTO.getWinner();
        long loserId = matchEloRequestDTO.getLoser();

        // Get elo of players
        int winnerElo = restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + winnerId,
            Integer.class);
        int loserElo = restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + loserId,
            Integer.class);

        // Calculate new elo of the two players
        int[] changedElo = calculateEloChange(winnerElo, loserElo);
        int newWinnerElo = changedElo[0];
        int newLoserElo = changedElo[1];

        // Save Elo History
        EloHistoryRequestDTO winnerHistory = new EloHistoryRequestDTO(winnerId, newWinnerElo, Reason.WIN);
        EloHistoryRequestDTO loserHistory = new EloHistoryRequestDTO(loserId, newLoserElo, Reason.LOSS);
        saveEloHistory(winnerElo, winnerHistory, LocalDateTime.now());  
        saveEloHistory(loserElo, loserHistory, LocalDateTime.now());

        // Update Player's Elo
        String updateServiceUrl = playersServiceUrl + "/api/player/winLossElo" ;
    
        WinLossUpdateDTO winDto = new WinLossUpdateDTO(winnerId, newWinnerElo, true);
        WinLossUpdateDTO lossDto = new WinLossUpdateDTO(loserId, newLoserElo, false);
        restTemplate.put(updateServiceUrl, winDto);
        restTemplate.put(updateServiceUrl, lossDto);
        
        // return positive response
        return "Players' elo updated successfully";
    }

    public EloHistoryChartDTO[] findPlayerEloHistoryForChart(long playerId) {
        List<EloHistory> eloHistoryList = eloRepository.findLatestEloHistoryByPlayerId(playerId);

        if(eloHistoryList.isEmpty()) {
            throw new PlayerHistoryNotFoundException("Player with player id " + playerId + " has no history");
        }
        
        List<EloHistoryChartDTO> chartDTOList = convertEloHistoriesToEloHistoryChartDTOs(eloHistoryList);

        return chartDTOList.toArray(new EloHistoryChartDTO[chartDTOList.size()]);
    }
    

    // Below are helper functions to convert entries to DTOs and vice versa
    public EloResponseDTO convertEntryToResponseDTO(EloHistory eloHistory) {
        EloResponseDTO eloResponseDTO = new EloResponseDTO();
        
        eloResponseDTO.setPlayerId(eloHistory.getPlayerId());
        eloResponseDTO.setOldElo(eloHistory.getOldElo());
        eloResponseDTO.setNewElo(eloHistory.getNewElo());
        eloResponseDTO.setCreatedAt(eloHistory.getCreatedAt());
        eloResponseDTO.setChangeReason(eloHistory.getChangeReason());
        return eloResponseDTO;
    }

    public List<EloResponseDTO> convertEntriesToResponseDTOs(List<EloHistory> eloHistoryList) {
        List<EloResponseDTO> responses = new ArrayList<>();
        for(EloHistory eloHistory : eloHistoryList) {
            responses.add(convertEntryToResponseDTO(eloHistory));
        }

        return responses;
    }

    public List<EloHistoryChartDTO> convertEloHistoriesToEloHistoryChartDTOs(List<EloHistory> eloHistoryList) {
        List<EloHistoryChartDTO> chartDTOs = eloHistoryList.stream()
                        .map(history -> new EloHistoryChartDTO(history.getNewElo(), history.getCreatedAt().toLocalDate()))
                        .sorted(Comparator.comparing(EloHistoryChartDTO::getDate))
                        .collect(Collectors.toList());
        return chartDTOs;
    }
}