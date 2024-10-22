package com.chess.tms.match_service.service;

import com.chess.tms.match_service.dto.MatchDTO;
import com.chess.tms.match_service.dto.MatchEloRequestDTO;
import com.chess.tms.match_service.dto.PlayerDetailsDTO;
import com.chess.tms.match_service.dto.TournamentDTO;
import com.chess.tms.match_service.dto.TournamentPlayerEloDTO;
import com.chess.tms.match_service.exception.GameTypeNotFoundException;
import com.chess.tms.match_service.exception.MatchDoesNotExistException;
import com.chess.tms.match_service.exception.PlayerDoesNotExistInMatchException;
import com.chess.tms.match_service.exception.MatchAlreadyCompletedException;
import com.chess.tms.match_service.exception.RoundTypeNotFoundException;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchesRepository matchRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    private final RestTemplate restTemplate;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    @Value("${elo.service.url}")
    private String eloServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Long createInitialMatches(Long tournamentId, Long gameTypeId) {
        // Get the list of players with Elo ratings from the tournament service
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                TournamentPlayerEloDTO[].class);
    
        TournamentPlayerEloDTO[] players = response.getBody();
        int totalPlayers = players.length;
    
        // Calculate the next power of 2 for the knockout stage
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;
    
        RoundType currentRoundType = getRoundType(nextPowerOfTwo);
    
        // Sort players by their Elo rating (descending order for best players first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());
    
        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();
    
        // Handle byes for top players first
        System.out.println("Handle Byes");
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            TournamentPlayerEloDTO playerWithBye = players[i];  // Top player gets a bye
            Match byeMatch = new Match();
            byeMatch.setTournamentId(tournamentId);
            byeMatch.setPlayer1Id(null);
            byeMatch.setPlayer2Id(null);
            byeMatch.setWinnerId(playerWithBye.getId());
            byeMatch.setRoundType(currentRoundType);
            byeMatch.setGameType(gameTypeRepository.findById(gameTypeId)
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            byeMatch.setStatus(Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);

            System.out.println("Player with bye: " + playerWithBye.getId());
            System.out.println("Player with bye: " + playerWithBye.getEloRating());
        }
        System.out.println();
    
        // Create first round matches with the remaining players after the byes
        List<Match> firstRoundMatches = new ArrayList<>();
        int lowIndex = byes; // Start pairing from the players after the byes
        int highIndex = totalPlayers - 1;
    
        System.out.println("Generate First Round Matches");
        // Pair the remaining players based on highest vs lowest elo
        while (lowIndex < highIndex) {
            TournamentPlayerEloDTO player1 = players[lowIndex]; // Higher Elo
            TournamentPlayerEloDTO player2 = players[highIndex]; // Lower Elo
    
            Match match = new Match();
            match.setTournamentId(tournamentId);
            match.setPlayer1Id(player1.getId());
            match.setPlayer2Id(player2.getId());
            match.setRoundType(currentRoundType);
            match.setGameType(gameTypeRepository.findById(gameTypeId)
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            match.setStatus(Match.MatchStatus.PENDING);
            firstRoundMatches.add(match);
            allMatches.add(match);
    
            lowIndex++;
            highIndex--;
            System.out.println("Player 1: " + player1.getId() + " vs Player 2: " + player2.getId());
            System.out.println("Player 1: " + player1.getEloRating() + " vs Player 2: " + player2.getEloRating());
        }
    
        // Combine first-round matches and bye matches
        List<Match> combinedFirstRoundMatches = new ArrayList<>(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);
        roundMatchesMap.put(1, combinedFirstRoundMatches);
    
        // Generate matches for subsequent rounds
        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;
        System.out.println();
        System.out.println("Generate Subsequent Rounds");
        while (currentRoundSize >= 1) {
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                System.out.println("Round " + roundNumber + " Match " + i);
                Match nextRoundMatch = new Match();
                nextRoundMatch.setTournamentId(tournamentId);
                nextRoundMatch.setRoundType(getRoundType(currentRoundSize * 2));
                nextRoundMatch.setGameType(gameTypeRepository.findById(gameTypeId)
                        .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
                nextRoundMatch.setStatus(Match.MatchStatus.PENDING);
                currentRoundMatches.add(nextRoundMatch);
                allMatches.add(nextRoundMatch);
            }
            roundMatchesMap.put(roundNumber, currentRoundMatches);
            currentRoundSize /= 2;
            roundNumber++;
        }
    
        // Save all the matches (both regular and bye matches)
        matchRepository.saveAll(allMatches);
    
        // Update next_match_id for each match in subsequent rounds
        for (int round = 1; round < roundNumber - 1; round++) {
            List<Match> previousRoundMatches = roundMatchesMap.get(round);
            List<Match> nextRoundMatches = roundMatchesMap.get(round + 1);
    
            for (int i = 0; i < previousRoundMatches.size(); i += 2) {
                Match previousMatch1 = previousRoundMatches.get(i);
                Match previousMatch2 = previousRoundMatches.get(i + 1);
                Match nextRoundMatch = nextRoundMatches.get(i / 2);
    
                previousMatch1.setNextMatchId(nextRoundMatch.getId());
                previousMatch2.setNextMatchId(nextRoundMatch.getId());
                matchRepository.save(previousMatch1);
                matchRepository.save(previousMatch2);
            }
        }
    
        return currentRoundType.getId();
    }

    // Get the RoundType based on the number of players
    private RoundType getRoundType(int numberOfPlayers) {
        return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
                .orElseThrow(() -> new RoundTypeNotFoundException(
                        "Round type not found for " + numberOfPlayers + " players"));
    }

    private void updateCurrentRoundForTournament(Long tournamentId, Long roundTypeId) {
        String updateRoundUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/round/" + roundTypeId;
        // Call the endpoint to update the current round
        restTemplate.put(updateRoundUrl, null);
    }

    public List<MatchDTO> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                      .map(this::convertMatchToMatchDTO)
                      .collect(Collectors.toList());
    }

    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));
        return convertMatchToMatchDTO(match);
    }

    public String advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        if(match.getWinnerId() != null) {
            throw new MatchAlreadyCompletedException("Match has already been completed");
        }

        if (!match.getPlayer1Id().equals(winnerId) && !match.getPlayer2Id().equals(winnerId)) {
            throw new PlayerDoesNotExistInMatchException(
                    "Player with id " + winnerId + " is not recognised in the match.");
        }


        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        // Update Elo of players

        MatchEloRequestDTO matchEloRequestDTO = new MatchEloRequestDTO();
        matchEloRequestDTO.setWinner(match.getWinnerId());
        matchEloRequestDTO.setLoser(match.getLoserId());
        restTemplate.put(eloServiceUrl + "/api/elo/match", matchEloRequestDTO);

        matchRepository.save(match);

        if (match.getNextMatchId() != null) {
            // Match nextMatch = matchRepository.findById(match.getNextMatchId())
            // .orElseThrow(() -> new MatchDoesNotExistException("Next match not found"));

            // if (nextMatch.getPlayer1Id() == null) {
            // nextMatch.setPlayer1Id(winnerId);
            // } else if (nextMatch.getPlayer2Id() == null) {
            // nextMatch.setPlayer2Id(winnerId);
            // }

            // matchRepository.save(nextMatch);

            // Check if round is completed
            RoundType roundType = roundTypeRepository.findById(match.getRoundType().getId())
                    .orElseThrow(
                            () -> new RoundTypeNotFoundException("Round type not found while checking next round"));

            List<Match> roundMatches = matchRepository.findByTournamentIdAndRoundTypeId(match.getTournamentId(),
                    roundType.getId());

            boolean roundCompleted = roundMatches.stream().allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);

            if (roundCompleted) {
                Match nextMatch = matchRepository.findById(match.getNextMatchId())
                        .orElseThrow(() -> new MatchDoesNotExistException("Next match not found"));

                updateCurrentRoundForTournament(match.getTournamentId(), nextMatch.getRoundType().getId());
                // Assign matches for the next round
                for (Match completedMatch : roundMatches) {
                    Long nextMatchId = completedMatch.getNextMatchId();
                    if (nextMatchId != null) {
                        Match advanceMatch = matchRepository.findById(nextMatchId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Next match not found for match ID: " + nextMatchId));
                        System.out.println(completedMatch.getWinnerId());
                        if (advanceMatch.getPlayer1Id() == null) {
                            advanceMatch.setPlayer1Id(completedMatch.getWinnerId());
                        } else if (advanceMatch.getPlayer2Id() == null) {
                            advanceMatch.setPlayer2Id(completedMatch.getWinnerId());
                        }
                        matchRepository.save(advanceMatch);
                    }
                }

                return "Tournament has advanced to the next round";
            }
            return "Winner advanced to the next round";
        } else {
            String updateTournamentUrl = tournamentServiceUrl + "/api/tournaments/" + match.getTournamentId()
                    + "/winner/" + winnerId;
            restTemplate.put(updateTournamentUrl, null);
            return "Tournament completed";
        }
    }

    public List<MatchDTO> getRecentMatchesByPlayerId(Long playerId) {

        List<Match> matches = matchRepository.findTop5ByPlayer1IdOrPlayer2IdOrderByUpdatedAtDesc(playerId, playerId);

        List<MatchDTO> matchDTOs = matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());

        return matchDTOs;
    }

    private MatchDTO convertMatchToMatchDTO(Match match) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());
        PlayerDetailsDTO player1 = new PlayerDetailsDTO();
        PlayerDetailsDTO player2 = new PlayerDetailsDTO();
        

        if(match.getPlayer1Id() != null){
            player1 = getPlayerDetails(match.getPlayer1Id());
            matchDTO.setPlayer1(player1);
        } else{
            matchDTO.setPlayer1(player1);
        }

        if(match.getPlayer2Id() != null){
            player2 = getPlayerDetails(match.getPlayer2Id());
            matchDTO.setPlayer2(player2);
        } else{
            matchDTO.setPlayer2(null);
        }
        if(match.getWinnerId() != null){
            matchDTO.setWinnerId(match.getWinnerId());
        } else{
            matchDTO.setWinnerId(null);
        }

        if(match.getLoserId() != null){
            matchDTO.setLoserId(match.getLoserId());
        } else{
            matchDTO.setLoserId(null);
        }

        matchDTO.setRoundType(match.getRoundType());
        matchDTO.setGameType(match.getGameType());
        matchDTO.setDate(match.getUpdatedAt());

        TournamentDTO tournamentDTO = getTournamentDetails(match.getTournamentId());
        matchDTO.setTournament(tournamentDTO);

        return matchDTO;
    }

    private TournamentDTO getTournamentDetails(Long tournamentId) {
        String tournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId;
        return restTemplate.getForObject(tournamentUrl, TournamentDTO.class);
    }

    private PlayerDetailsDTO getPlayerDetails(long playerId) {
        ResponseEntity<PlayerDetailsDTO> response = restTemplate.getForEntity(
            playerServiceUrl + "/api/player/" + playerId,
            PlayerDetailsDTO.class);
        
        return response.getBody();
    }
}
