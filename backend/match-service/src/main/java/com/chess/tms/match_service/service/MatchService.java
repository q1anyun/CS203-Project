package com.chess.tms.match_service.service;

import com.chess.tms.match_service.dto.MatchDTO;
import com.chess.tms.match_service.dto.TournamentDTO;
import com.chess.tms.match_service.dto.TournamentPlayerDTO;
import com.chess.tms.match_service.dto.TournamentPlayerEloDTO;
import com.chess.tms.match_service.exception.GameTypeNotFoundException;
import com.chess.tms.match_service.exception.MatchDoesNotExistException;
import com.chess.tms.match_service.exception.PlayerDoesNotExistInMatchException;
import com.chess.tms.match_service.exception.RoundTypeNotFoundException;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void createInitialMatches(Long tournamentId, Long gameTypeId) {
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
            tournamentServiceUrl+"/api/tournaments/"+tournamentId+"/players", HttpMethod.GET, null, TournamentPlayerEloDTO[].class);
        
        TournamentPlayerEloDTO[] players = response.getBody();
        int totalPlayers = players.length;

        // Calculate the next power of 2 for the knockout stage
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;

        // Sort players by their Elo rating (descending order for best players first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());

        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();

        // Create first round matches
        List<Match> firstRoundMatches = new ArrayList<>();
        for (int i = 0; i < totalPlayers - byes; i += 2) {
            TournamentPlayerEloDTO player1 = players[i];
            TournamentPlayerEloDTO player2 = players[i + 1];

            Match match = new Match();
            match.setTournamentId(tournamentId);
            match.setPlayer1Id(player1.getId());
            match.setPlayer2Id(player2.getId());
            match.setRoundType(getRoundType(nextPowerOfTwo));
            match.setGameType(gameTypeRepository.findById(gameTypeId)
                .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            match.setStatus(Match.MatchStatus.PENDING);
            firstRoundMatches.add(match);
            allMatches.add(match);
        }

        // Handle byes for top players
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            TournamentPlayerEloDTO playerWithBye = players[totalPlayers - 1 - i];
            Match byeMatch = new Match();
            byeMatch.setTournamentId(tournamentId);
            byeMatch.setPlayer1Id(null);
            byeMatch.setPlayer2Id(null);
            byeMatch.setWinnerId(playerWithBye.getId());
            byeMatch.setRoundType(getRoundType(nextPowerOfTwo));
            byeMatch.setGameType(gameTypeRepository.findById(gameTypeId)
                .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            byeMatch.setStatus(Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);
        }

        List<Match> combinedFirstRoundMatches = new ArrayList<>();

        combinedFirstRoundMatches.addAll(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);

        roundMatchesMap.put(1, combinedFirstRoundMatches);

        // Generate the remaining rounds
        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;

        while (currentRoundSize >= 1) {
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
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

        matchRepository.saveAll(allMatches);

        // Update next_match_id for each match
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
    }

    // Get the RoundType based on the number of players
    private RoundType getRoundType(int numberOfPlayers) {
    return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
            .orElseThrow(() -> new RoundTypeNotFoundException("Round type not found for " + numberOfPlayers + " players"));
}

    public List<Match> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    public Match getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        return match;
    }

    // To do: Add check whether winnerId is part of the match
    public void advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        if (match.getPlayer1Id() != winnerId || match.getPlayer2Id() != winnerId) {
            throw new PlayerDoesNotExistInMatchException(
                    "Player with id " + winnerId + " is not recognised in the match.");
        }

        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        matchRepository.save(match);

        if (match.getNextMatchId() != null) {
            Match nextMatch = matchRepository.findById(match.getNextMatchId())
                    .orElseThrow(() -> new RuntimeException("Next match not found"));

            if (nextMatch.getPlayer1Id() == null) {
                nextMatch.setPlayer1Id(winnerId);
            } else if (nextMatch.getPlayer2Id() == null) {
                nextMatch.setPlayer2Id(winnerId);
            }

            matchRepository.save(nextMatch);
        }
    }

    public void generateNextRound(Long tournamentId) {
        // Find the most recent completed round (the round with the minimum number of
        // completed matches)
        // Might store current Round id in Tourmanent table instead of finding it every
        // time
        RoundType mostRecentCompletedRound = roundTypeRepository.findMostRecentCompletedRound(tournamentId)
                .orElseThrow(() -> new RuntimeException("No completed rounds found for this tournament."));

        // Get the matches for that completed round
        List<Match> completedMatches = matchRepository.findCompletedMatchesByRoundType(tournamentId,
                mostRecentCompletedRound.getId());

        // Assign matches for the next round
        for (Match completedMatch : completedMatches) {
            Long nextMatchId = completedMatch.getNextMatchId();
            if (nextMatchId != null) {
                Match nextMatch = matchRepository.findById(nextMatchId)
                        .orElseThrow(() -> new RuntimeException("Next match not found for match ID: " + nextMatchId));
                if (nextMatch.getPlayer1Id() == null) {
                    nextMatch.setPlayer1Id(completedMatch.getWinnerId());
                } else if (nextMatch.getPlayer2Id() == null) {
                    nextMatch.setPlayer2Id(completedMatch.getWinnerId());
                }

                matchRepository.save(nextMatch);
            }
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
        matchDTO.setWinnerId(match.getWinnerId());
        matchDTO.setLoserId(match.getLoserId());
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
}
