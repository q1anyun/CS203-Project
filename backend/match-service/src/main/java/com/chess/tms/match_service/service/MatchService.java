package com.chess.tms.match_service.service;

import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.MatchesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.MatchResult;

@Service
public class MatchService {

    @Autowired
    private MatchesRepository matchRepository;

    private final RestTemplate restTemplate;

    @Value("${tournament.service.url}")
    private String tournamentServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Creates the initial matches for a tournament
    public void createInitialMatches(Long tournamentId) {
        // Fetch the tournament and its players from the Tournament microservice
        String playersUrl = tournamentServiceUrl + "/" + tournamentId + "/players";
        TournamentPlayerDTO[] players = restTemplate.getForObject(playersUrl, TournamentPlayerDTO[].class);
        int totalPlayers = players.length;

        // Calculate the next power of 2 for the knockout stage
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;

        // Sort players by their Elo rating
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerDTO::getEloRating));

        // Create first-round matches
        List<Match> matches = new ArrayList<>();
        int roundNumber = 1;

        for (int i = 0; i < totalPlayers - byes; i += 2) {
            TournamentPlayerDTO player1 = players[i];
            TournamentPlayerDTO player2 = players[i + 1];

            Match match = new Match();
            match.setTournamentId(tournamentId);
            match.setPlayer1Id(player1.getId());
            match.setPlayer2Id(player2.getId());
            match.setRoundType(getRoundType(roundNumber));
            match.setStatus(Match.MatchStatus.PENDING);
            matches.add(match);
        }

        // Save all matches
        matchRepository.saveAll(matches);

        // Handle byes for top players
        for (int i = 0; i < byes; i++) {
            TournamentPlayerDTO playerWithBye = players[totalPlayers - 1 - i];
            Match byeMatch = new Match();
            byeMatch.setTournamentId(tournamentId);
            byeMatch.setPlayer1Id(playerWithBye.getId());
            byeMatch.setPlayer2Id(null); // No opponent
            byeMatch.setRoundType(getRoundType(roundNumber));
            byeMatch.setStatus(Match.MatchStatus.COMPLETED); // Auto-win for player
            matchRepository.save(byeMatch);
        }
    }

    public void advanceWinner(MatchResult matchResult) {
        Match match = matchRepository.findById(matchResult.getMatchId()).orElseThrow(() -> new RuntimeException("Match not found"));
        match.setStatus(Match.MatchStatus.COMPLETED);
        matchRepository.save(match);

        // Advance the winner to the next round
        Optional<Match> nextMatchOptional = matchRepository.findById(match.getNextMatchId());
        if (nextMatchOptional.isPresent()) {
            Match nextMatch = nextMatchOptional.get();
            if (nextMatch.getPlayer1() == null) {
                nextMatch.setPlayer1(matchResult.getWinner());
            } else if (nextMatch.getPlayer2() == null) {
                nextMatch.setPlayer2(matchResult.getWinner());
            }
            matchRepository.save(nextMatch);
        }
    }

    private RoundType getRoundType(int roundNumber) {
        // Fetch round type from a predefined list (can be from another service or a database)
        return new RoundType((long) roundNumber, "Round " + roundNumber);
    }
}
