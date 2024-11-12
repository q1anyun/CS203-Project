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
import com.chess.tms.match_service.exception.SwissBracketNotFoundException;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.model.Match.MatchStatus;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;
import com.chess.tms.match_service.repository.SwissBracketRepository;
import com.chess.tms.match_service.repository.SwissStandingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import java.util.concurrent.ExecutionException;

@Service
public class MatchService {

    @Autowired
    private MatchesRepository matchRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private SwissBracketRepository swissBracketRepository;

    @Autowired
    private SwissStandingRepository swissStandingRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    @Value("${elo.service.url}")
    private String eloServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Creates knockout matches for a tournament based on the number of players.
     * Matches are paired based on Elo ratings with high-rated players facing
     * lower-rated players.
     *
     * @param tournamentId      the tournament ID
     * @param gameTypeId        the game type ID
     * @param advancedPlayerIds list of players advancing from previous rounds, or
     *                          null if initial round
     * @return ID of the initial round's RoundType
     */
    public Long createKnockoutMatches(Long tournamentId, Long gameTypeId, List<Long> advancedPlayerIds) {
        TournamentPlayerEloDTO[] players;
        // If advancedPlayerIds is null, get the list of players with Elo ratings from
        // the tournament service
        if (advancedPlayerIds == null) {
            ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                    tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                    TournamentPlayerEloDTO[].class);
            players = response.getBody();
        } else {
            // If advancedPlayerIds is not null, get the list of players from the player
            // service
            players = advancedPlayerIds.stream()
                    .map(playerId -> restTemplate.getForObject(playerServiceUrl + "/api/player/" + playerId,
                            PlayerDetailsDTO.class))
                    .map(player -> new TournamentPlayerEloDTO(player.getId(), player.getEloRating()))
                    .toArray(TournamentPlayerEloDTO[]::new);
        }

        // Calculate the next power of 2 for the knockout structure
        // This is done by finding the smallest power of 2 that is greater than the
        // number of players
        int totalPlayers = players.length;
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        // Calculate the number of byes required for the top players
        int byes = nextPowerOfTwo - totalPlayers;
        // Get the round type for the next round
        RoundType currentRoundType = getRoundType(nextPowerOfTwo);

        // Sort players by Elo rating (highest first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());

        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();

        // Map to store matches for each round
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();

        // Handle byes for top players first
        List<Match> byeMatches = getByeMatches(tournamentId, gameTypeId, players, byes, currentRoundType, allMatches);

        // Create first round matches with the remaining players after the byes
        List<Match> firstRoundMatches = new ArrayList<>();

        // Set the low and high indices for the first round matches
        int lowIndex = byes;
        int highIndex = totalPlayers - 1;

        // Generate the first round matches
        generateFirstRoundMatches(tournamentId, gameTypeId, players, currentRoundType, allMatches, firstRoundMatches,
                lowIndex, highIndex);

        // Combine first-round and bye matches, add to map, and generate subsequent
        // rounds
        List<Match> combinedFirstRoundMatches = new ArrayList<>(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);

        // Add the first round matches to the map
        roundMatchesMap.put(1, combinedFirstRoundMatches);

        // Calculate the current round size and round number
        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;

        // Generate subsequent rounds
        roundNumber = generateSubsequentMatches(tournamentId, gameTypeId, allMatches, roundMatchesMap, currentRoundSize,
                roundNumber);

        // Save all the matches (both regular and bye matches) and update next_match_id
        // for each match in subsequent rounds
        matchRepository.saveAll(allMatches);

        // Update next_match_id for each match in subsequent rounds
        updateNextMatchId(roundMatchesMap, roundNumber);

        return currentRoundType.getId();
    }

    /**
     * Generates the first round of knockout matches by pairing players based on Elo
     * ratings.
     */
    private void generateFirstRoundMatches(Long tournamentId, Long gameTypeId, TournamentPlayerEloDTO[] players,
            RoundType currentRoundType, List<Match> allMatches, List<Match> firstRoundMatches, int lowIndex,
            int highIndex) {

        // Generate matches for the first round by pairing players based on Elo ratings
        // The pairing is done by pairing the lowest rated player with the highest rated
        // player
        // Pairing logic is done using left and right pointers to traverse the array -
        // complexity O(n)
        while (lowIndex < highIndex) {
            TournamentPlayerEloDTO player1 = players[lowIndex];
            TournamentPlayerEloDTO player2 = players[highIndex];

            // Create the match for the first round
            Match match = createMatch(tournamentId, player1.getId(), player2.getId(), null, null, null,
                    currentRoundType, gameTypeId, null, Match.MatchStatus.PENDING);

            // Add the match to the list of first round matches
            firstRoundMatches.add(match);
            // Add the match to the list of all matches
            allMatches.add(match);

            lowIndex++;
            highIndex--;

        }
    }

    /**
     * Generates subsequent rounds by creating matches for each successive round
     * until the final match.
     */
    private int generateSubsequentMatches(Long tournamentId, Long gameTypeId, List<Match> allMatches,
            Map<Integer, List<Match>> roundMatchesMap, int currentRoundSize, int roundNumber) {
        // Generate subsequent rounds until the current round size is 1
        while (currentRoundSize >= 1) {
            // Create a list to store the matches for the current round
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                // Get the round type for the next round
                RoundType roundType = getRoundType(currentRoundSize * 2);

                // Create the match for the next round
                Match nextRoundMatch = createMatch(tournamentId, null, null, null, null, null, roundType, gameTypeId,
                        null, Match.MatchStatus.PENDING);
                currentRoundMatches.add(nextRoundMatch);
                allMatches.add(nextRoundMatch);
            }
            // Add the current round matches to the map
            roundMatchesMap.put(roundNumber, currentRoundMatches);
            // Update the current round size and round number
            currentRoundSize /= 2;
            // Increment the round number
            roundNumber++;
        }
        // Return the final round number after all rounds are generated
        // This is used to update the next_match_id for each match in subsequent rounds
        return roundNumber;
    }

    /**
     * Updates next_match_id for each match in subsequent rounds to maintain
     * knockout structure.
     */
    private void updateNextMatchId(Map<Integer, List<Match>> roundMatchesMap, int roundNumber) {
        // Update next_match_id for each match in subsequent rounds
        for (int round = 1; round < roundNumber - 1; round++) {
            // Get the matches for the previous round
            List<Match> previousRoundMatches = roundMatchesMap.get(round);
            // Get the matches for the next round
            List<Match> nextRoundMatches = roundMatchesMap.get(round + 1);

            // Update next_match_id for each match in the previous round
            for (int i = 0; i < previousRoundMatches.size(); i += 2) {
                // Get the matches for the previous round
                Match previousMatch1 = previousRoundMatches.get(i);
                Match previousMatch2 = previousRoundMatches.get(i + 1);
                // Get the matches for the next round
                Match nextRoundMatch = nextRoundMatches.get(i / 2);

                // Set the next_match_id for each match in the previous round
                previousMatch1.setNextMatchId(nextRoundMatch.getId());
                previousMatch2.setNextMatchId(nextRoundMatch.getId());
                // Save the matches
                matchRepository.save(previousMatch1);
                matchRepository.save(previousMatch2);
            }
        }
    }

    /**
     * Creates and returns matches for players receiving a bye in the first round.
     */
    private List<Match> getByeMatches(Long tournamentId, Long gameTypeId, TournamentPlayerEloDTO[] players, int byes,
            RoundType currentRoundType, List<Match> allMatches) {
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            // Top player gets a bye
            TournamentPlayerEloDTO playerWithBye = players[i];
            // Create the bye match
            Match byeMatch = createMatch(tournamentId, null, null, playerWithBye.getId(), null, null, currentRoundType,
                    gameTypeId, null, Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);

        }
        // Return the list of bye matches
        return byeMatches;
    }

    /**
     * Create matches for a tournament based on the Swiss tournament format.
     * In a Swiss tournament, players are paired with other players with similar
     * standings (based on wins).
     * Players accumulate points as they progress through the rounds without being
     * eliminated.
     *
     * @param tournamentId the ID of the tournament
     * @param gameTypeId   the ID of the game type
     */
    public Long createSwissMatches(Long tournamentId, Long gameTypeId) {
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                TournamentPlayerEloDTO[].class);

        // Get the list of players with Elo ratings from the tournament service
        TournamentPlayerEloDTO[] players = response.getBody();
        // Get the number of players in the tournament
        int totalPlayers = players.length;
        // Create the Swiss bracket for the tournament
        SwissBracket bracket = createSwissBracket(tournamentId, totalPlayers);
        // Set the standings for each player in the tournament
        setSwissStanding(players, bracket);
        // Create the matches for the first round
        createRoundMatches(players, tournamentId, gameTypeId, bracket.getCurrentRound());
        // Return the ID of the Swiss bracket
        return bracket.getId();
    }

    /**
     * Initializes standings for each player in a Swiss bracket.
     *
     * @param players array of players in the tournament
     * @param bracket the Swiss bracket for the tournament
     */
    private void setSwissStanding(TournamentPlayerEloDTO[] players, SwissBracket bracket) {
        // Set the standings for each player in the tournament
        for (TournamentPlayerEloDTO player : players) {
            SwissStanding standing = new SwissStanding();
            standing.setBracket(bracket);
            standing.setTournamentPlayerId(player.getId());
            standing.setPlayerId(player.getId());
            standing.setWins(0);
            standing.setLosses(0);
            swissStandingRepository.save(standing);
        }
    }

    /**
     * Creates and saves a SwissBracket with the appropriate number of rounds based
     * on total players.
     *
     * @param tournamentId the ID of the tournament
     * @param totalPlayers the number of players in the tournament
     * @return the created SwissBracket
     */
    private SwissBracket createSwissBracket(Long tournamentId, int totalPlayers) {
        SwissBracket bracket = new SwissBracket();
        bracket.setTournamentId(tournamentId);
        bracket.setCurrentRound(1);
        bracket.setNumberOfRounds((int) Math.ceil(Math.log(totalPlayers) / Math.log(2)) + 1); // Set number of rounds
        swissBracketRepository.save(bracket);
        return bracket;
    }

    /**
     * Creates matches between players for a Swiss round, ensuring that players with
     * similar standings are paired.
     * Players from each group (split based on Elo) are paired in a balanced way to
     * ensure fair competition.
     *
     * @param players      array of players participating in the round
     * @param tournamentId the tournament ID
     * @param gameTypeId   the game type ID
     * @param roundNumber  the current round number in the Swiss bracket
     */
    private void createRoundMatches(TournamentPlayerEloDTO[] players, Long tournamentId, Long gameTypeId,
            int roundNumber) {
        List<Match> matches = new ArrayList<>();
        Map<Long, Set<Long>> playerMatchHistory = getPreviousOpponents(tournamentId);

        // Convert players array to list for easier manipulation
        List<TournamentPlayerEloDTO> allPlayers = new ArrayList<>(Arrays.asList(players));

        // Find valid pairings with the assumption of even number of players
        List<Pairing> pairings = findValidPairings(allPlayers, playerMatchHistory);

        // Create matches from the pairings
        for (Pairing pairing : pairings) {
            Match match = createMatch(
                    tournamentId,
                    pairing.player1.getId(),
                    pairing.player2.getId(),
                    null, null, roundNumber,
                    getRoundTypeForSwissRound(),
                    gameTypeId,
                    null,
                    Match.MatchStatus.PENDING);
            matches.add(match);
        }

        // Save matches
        matchRepository.saveAll(matches);
    }

    // Helper class to represent a pairing
    private static class Pairing {
        TournamentPlayerEloDTO player1;
        TournamentPlayerEloDTO player2;

        Pairing(TournamentPlayerEloDTO p1, TournamentPlayerEloDTO p2) {
            this.player1 = p1;
            this.player2 = p2;
        }
    }

    /**
     * Finds valid pairings for players in a Swiss tournament using a greedy
     * heuristic.
     * Prioritizes pairing players with fewer available opponents, reducing future
     * constraints.
     *
     * @param players            list of players to pair
     * @param playerMatchHistory map of player IDs to their match history
     * @return list of valid pairings
     */
    private List<Pairing> findValidPairings(List<TournamentPlayerEloDTO> players,
            Map<Long, Set<Long>> playerMatchHistory) {
        // Shuffle players to add randomness
        Collections.shuffle(players);

        List<Pairing> allPairings = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(4); // Adjust the thread pool size

        // Divide players into sublists for parallel processing
        int groupSize = players.size() / 4;
        List<List<TournamentPlayerEloDTO>> subLists = new ArrayList<>();

        for (int i = 0; i < players.size(); i += groupSize) {
            subLists.add(players.subList(i, Math.min(i + groupSize, players.size())));
        }

        // Create a list of CompletableFuture objects for each sublist
        // Each CompletableFuture object is used to find pairings for a sublist in
        // parallel
        // The findPairingsForGroup method is called with a supplier that returns the
        // result of findPairingsForGroup
        // The supplier is executed asynchronously using CompletableFuture.supplyAsync
        // The result of findPairingsForGroup is a list of Pairing objects
        List<CompletableFuture<List<Pairing>>> futures = new ArrayList<>();

        for (List<TournamentPlayerEloDTO> subList : subLists) {
            futures.add(
                    CompletableFuture.supplyAsync(() -> findPairingsForGroup(subList, playerMatchHistory), executor));
        }

        // Wait for all the CompletableFuture objects to complete and add the pairings
        // to the allPairings list
        for (CompletableFuture<List<Pairing>> future : futures) {
            try {
                allPairings.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        return allPairings;
    }

    /**
     * Helper method to find pairings for a specific group of players.
     * 
     * @param players            list of players in a group
     * @param playerMatchHistory match history to avoid repeat pairings
     * @return list of valid pairings for the group
     */
    private List<Pairing> findPairingsForGroup(List<TournamentPlayerEloDTO> players,
            Map<Long, Set<Long>> playerMatchHistory) {
        List<Pairing> pairings = new ArrayList<>();
        Set<Long> pairedPlayerIds = new HashSet<>();

        for (TournamentPlayerEloDTO player1 : players) {
            if (pairedPlayerIds.contains(player1.getId()))
                continue;

            TournamentPlayerEloDTO bestMatch = null;
            int minOpponentCount = Integer.MAX_VALUE;

            // Find the best available match for player1 with no previous pairing
            for (TournamentPlayerEloDTO player2 : players) {
                if (player1.getId().equals(player2.getId()) || pairedPlayerIds.contains(player2.getId()))
                    continue;

                // Check if player1 and player2 have not played before
                if (!hasPlayedBefore(playerMatchHistory, player1.getId(), player2.getId())) {
                    int opponentCount = playerMatchHistory.getOrDefault(player2.getId(), Collections.emptySet()).size();
                    if (opponentCount < minOpponentCount) {
                        minOpponentCount = opponentCount;
                        bestMatch = player2;
                    }
                }
            }

            // If a unique match was found, add it; otherwise, add the best available match
            // to avoid unpaired players
            if (bestMatch != null) {
                pairings.add(new Pairing(player1, bestMatch));
                pairedPlayerIds.add(player1.getId());
                pairedPlayerIds.add(bestMatch.getId());
            } else {
                // Fallback logic: try to find any remaining unmatched player if no unique
                // pairing is possible
                for (TournamentPlayerEloDTO player2 : players) {
                    if (!player1.getId().equals(player2.getId()) && !pairedPlayerIds.contains(player2.getId())) {
                        pairings.add(new Pairing(player1, player2));
                        pairedPlayerIds.add(player1.getId());
                        pairedPlayerIds.add(player2.getId());
                        break;
                    }
                }
            }
        }
        return pairings;
    }

    /**
     * Retrieves the match history for each player in the tournament to avoid
     * repeated pairings.
     *
     * @param tournamentId the ID of the tournament
     * @return a map where each player is mapped to a set of past opponents
     */
    private Map<Long, Set<Long>> getPreviousOpponents(Long tournamentId) {

        List<Match> previousMatches = matchRepository.findByTournamentId(tournamentId);
        Map<Long, Set<Long>> playerMatchHistory = new HashMap<>();

        // Iterate through the previous matches
        for (Match match : previousMatches) {
            // Get the player IDs for the match
            Long player1Id = match.getPlayer1Id();
            Long player2Id = match.getPlayer2Id();

            // Ensure both player IDs are present in the map
            playerMatchHistory.putIfAbsent(player1Id, new HashSet<>());
            playerMatchHistory.putIfAbsent(player2Id, new HashSet<>());

            // Record the match between the two players
            playerMatchHistory.get(player1Id).add(player2Id);
            playerMatchHistory.get(player2Id).add(player1Id);
        }

        return playerMatchHistory;
    }

    /**
     * Retrieve the RoundType based on the number of players, used to determine
     * the number of rounds or type of knockout format required for the tournament.
     * 
     * @param numberOfPlayers the number of players in the round
     * @return the RoundType matching the number of players
     */
    private RoundType getRoundType(int numberOfPlayers) {
        return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
                .orElseThrow(() -> new RoundTypeNotFoundException(
                        "Round type not found for " + numberOfPlayers + " players"));
    }

    /**
     * Retrieve the RoundType specific to the Swiss tournament format.
     * 
     * @return the RoundType corresponding to a Swiss tournament
     */
    private RoundType getRoundTypeForSwissRound() {
        return roundTypeRepository.findByRoundName("Swiss")
                .orElseThrow(() -> new RoundTypeNotFoundException("Round type not found for Swiss"));
    }

    /**
     * Updates the current round of the tournament by calling the external
     * tournament service.
     *
     * @param tournamentId the ID of the tournament
     * @param roundTypeId  the ID of the round type to set as the current round
     */
    private void updateCurrentRoundForTournament(Long tournamentId, Long roundTypeId) {
        String updateRoundUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/round/" + roundTypeId;
        // Call the endpoint to update the current round
        restTemplate.put(updateRoundUrl, null);
    }

    /**
     * Retrieve all matches for a specific tournament and convert them into a list
     * of MatchDTO objects.
     *
     * @param tournamentId the tournament ID
     * @return a list of MatchDTO objects for the specified tournament
     */
    public List<MatchDTO> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single match by its ID and converts it to a MatchDTO.
     *
     * @param matchId the ID of the match
     * @return the MatchDTO representation of the match
     */
    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));
        return convertMatchToMatchDTO(match);
    }

    /**
     * Advances the winner of a match, updates Elo ratings, and progresses the
     * tournament
     * to the next round if applicable (Swiss or Knockout).
     *
     * @param matchId  the ID of the match
     * @param winnerId the ID of the winning player
     * @return a message indicating the progress of the tournament
     */
    public String advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        // Validate the match and winner
        validateMatchAndWinner(winnerId, match);

        // Set winner and loser IDs, and mark the match as completed
        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        // Update Elo of players
        updateMatchEloRequestDTO(match);

        matchRepository.save(match);

        // Advance the match based on the tournament format
        return match.getRoundType().getRoundName().equals("Swiss")
                ? advanceSwissMatch(match, winnerId)
                : advanceKnockoutMatch(match, winnerId);
    }

    /**
     * Validates that the match has not been completed and the winner is a valid
     * participant.
     *
     * @param winnerId the ID of the winner
     * @param match    the match being validated
     */
    private void validateMatchAndWinner(Long winnerId, Match match) {
        // Validate that the match has not been completed
        if (match.getWinnerId() != null) {
            throw new MatchAlreadyCompletedException("Match has already been completed");
        }

        // Validate that the winner is a valid participant in the match
        if (!match.getPlayer1Id().equals(winnerId) && !match.getPlayer2Id().equals(winnerId)) {
            throw new PlayerDoesNotExistInMatchException(
                    "Player with id " + winnerId + " is not recognised in the match.");
        }
    }

    /**
     * Sends a request to update the Elo ratings of players based on the match
     * results.
     *
     * @param match the completed match with a winner and loser
     */
    private void updateMatchEloRequestDTO(Match match) {
        MatchEloRequestDTO matchEloRequestDTO = new MatchEloRequestDTO();
        matchEloRequestDTO.setWinner(match.getWinnerId());
        matchEloRequestDTO.setLoser(match.getLoserId());

        // Send the request to update the Elo ratings
        restTemplate.put(eloServiceUrl + "/api/elo/match", matchEloRequestDTO);
    }

    /**
     * Advances a Swiss tournament match, checking if the round is completed and
     * progressing
     * to the next round or moving to knockout phase if the Swiss rounds are
     * finished.
     *
     * @param match    the completed match
     * @param winnerId the ID of the match winner
     * @return a message indicating the progress of the Swiss tournament
     */
    private String advanceSwissMatch(Match match, Long winnerId) {
        SwissBracket bracket = swissBracketRepository.findByTournamentId(match.getTournamentId())
                .orElseThrow(() -> new SwissBracketNotFoundException("Swiss bracket not found"));

        // Update the Swiss standings for the winner and loser
        updateSwissStanding(match, winnerId, bracket);

        // Get the matches for the current round
        List<Match> roundMatches = matchRepository.findByTournamentIdAndSwissRoundNumber(match.getTournamentId(),
                bracket.getCurrentRound());

        // Check if the round is completed
        if (isRoundCompleted(roundMatches)) {
            // Check if the last Swiss round is reached
            if (bracket.getCurrentRound() == bracket.getNumberOfRounds()) {
                return completedSwissRound(match, bracket);
            } else {
                return nextSwissRound(match, bracket);
            }
        }
        return "Swiss standings updated";
    }

    /**
     * Updates the Swiss standings for the winner and loser of a match within a
     * Swiss bracket.
     *
     * @param match    the match that was completed
     * @param winnerId the ID of the winning player
     * @param bracket  the Swiss bracket to which the match belongs
     */
    private void updateSwissStanding(Match match, Long winnerId, SwissBracket bracket) {
        // Get the Swiss standing for the winner
        SwissStanding winnerStanding = swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), winnerId)
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));

        // Get the Swiss standing for the loser
        SwissStanding loserStanding = swissStandingRepository
                .findByBracketIdAndPlayerId(bracket.getId(), match.getLoserId())
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));

        // Update the wins and losses for the winner and loser
        winnerStanding.setWins(winnerStanding.getWins() + 1);
        loserStanding.setLosses(loserStanding.getLosses() + 1);

        swissStandingRepository.save(winnerStanding);
        swissStandingRepository.save(loserStanding);
    }

    /**
     * Proceeds to the next Swiss round by updating the current round and generating
     * new matches.
     *
     * @param match   the completed match
     * @param bracket the Swiss bracket to advance
     * @return a message indicating advancement to the next Swiss round
     */
    private String nextSwissRound(Match match, SwissBracket bracket) {
        // Update the current round
        bracket.setCurrentRound(bracket.getCurrentRound() + 1);
        swissBracketRepository.save(bracket);
        // Fetch all players with their updated standings for the next round
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + match.getTournamentId(),
                HttpMethod.GET, null, TournamentPlayerEloDTO[].class);

        TournamentPlayerEloDTO[] allPlayers = response.getBody();

        // Create the matches for the next round
        createRoundMatches(allPlayers, match.getTournamentId(), match.getGameType().getId(),
                bracket.getCurrentRound());

        return "Advanced to Swiss Round " + bracket.getCurrentRound();
    }

    /**
     * Completes the Swiss rounds and initiates the knockout phase for the top
     * players.
     *
     * @param match   the last match in the Swiss phase
     * @param bracket the Swiss bracket for the tournament
     * @return a message indicating the tournament has moved to knockout phase
     */
    private String completedSwissRound(Match match, SwissBracket bracket) {
        List<SwissStanding> standings = getSortedStandings(bracket);
        int half = standings.size() / 2;
        int advancingScoreThreshold = getAdvancingScoreThreshold(standings, half);

        List<SwissStanding> potentialAdvancers = getPotentialAdvancers(standings, advancingScoreThreshold);

        List<Long> advancedPlayers = resolveAdvancers(potentialAdvancers, advancingScoreThreshold, half, match);

        // Create knockout matches for the selected players and update the tournament
        // round
        Long currentRoundId = createKnockoutMatches(match.getTournamentId(), match.getGameType().getId(),
                advancedPlayers);
        updateCurrentRoundForTournament(match.getTournamentId(), currentRoundId);

        return "Swiss rounds completed, moving to knockout phase.";
    }

    /**
     * Fetches the sorted standings by wins and losses.
     */
    private List<SwissStanding> getSortedStandings(SwissBracket bracket) {
        return swissStandingRepository.findByBracketIdOrderByWinsDescLossesAsc(bracket.getId());
    }

    /**
     * Determines the minimum score needed to advance.
     */
    private int getAdvancingScoreThreshold(List<SwissStanding> standings, int half) {
        return standings.get(half - 1).getWins();
    }

    /**
     * Collects players with scores meeting or exceeding the threshold for
     * advancing.
     */
    private List<SwissStanding> getPotentialAdvancers(List<SwissStanding> standings, int threshold) {
        List<SwissStanding> potentialAdvancers = new ArrayList<>();
        for (SwissStanding player : standings) {
            if (player.getWins() >= threshold) {
                potentialAdvancers.add(player);
            }
        }
        return potentialAdvancers;
    }

    /**
     * Resolves which players will advance, using tiebreaker if necessary.
     */
    private List<Long> resolveAdvancers(List<SwissStanding> potentialAdvancers, int threshold, int half, Match match) {
        List<Long> advancedPlayers = new ArrayList<>();

        if (needsTieBreaker(potentialAdvancers, half)) {
            List<SwissStanding> tiedPlayers = getTiedPlayers(potentialAdvancers, threshold);
            Map<Long, Double> opponentDifficultyScores = calculateOpponentDifficultyScores(tiedPlayers,
                    match.getTournamentId());
            advancedPlayers = selectAdvancersWithTieBreaker(potentialAdvancers, tiedPlayers, opponentDifficultyScores,
                    threshold, half);
        } else {
            for (SwissStanding player : potentialAdvancers) {
                advancedPlayers.add(player.getPlayerId());
            }
        }
        return advancedPlayers;
    }

    /**
     * Checks if tie-breaking is needed based on the number of potential advancers.
     */
    private boolean needsTieBreaker(List<SwissStanding> potentialAdvancers, int half) {
        return potentialAdvancers.size() > half;
    }

    /**
     * Identifies players with scores equal to the advancing threshold for
     * tiebreaker.
     */
    private List<SwissStanding> getTiedPlayers(List<SwissStanding> potentialAdvancers, int threshold) {
        List<SwissStanding> tiedPlayers = new ArrayList<>();
        for (SwissStanding player : potentialAdvancers) {
            if (player.getWins() == threshold) {
                tiedPlayers.add(player);
            }
        }
        return tiedPlayers;
    }

    /**
     * Selects players to advance, resolving ties based on opponent difficulty.
     */
    private List<Long> selectAdvancersWithTieBreaker(List<SwissStanding> potentialAdvancers,
            List<SwissStanding> tiedPlayers, Map<Long, Double> opponentDifficultyScores, int threshold, int half) {

        System.out.println(opponentDifficultyScores);
        // Sort tied players by opponent difficulty in descending order
        Collections.sort(tiedPlayers, (p1, p2) -> Double.compare(opponentDifficultyScores.get(p2.getPlayerId()),
                opponentDifficultyScores.get(p1.getPlayerId())));
        // Print out the tied players sorted by opponent difficulty
        for (SwissStanding player : tiedPlayers) {
            System.out.println("Tied player: " + player.getPlayerId() + " with opponent difficulty score: "
                    + opponentDifficultyScores.get(player.getPlayerId()));
        }

        List<Long> advancedPlayers = new ArrayList<>();

        // Add players with scores above the threshold first
        for (SwissStanding player : potentialAdvancers) {
            if (player.getWins() > threshold) {
                advancedPlayers.add(player.getPlayerId());
            }
        }
        System.out.println("Advanced players: " + advancedPlayers);

        // Add remaining slots from the sorted tied list
        int remainingSlots = half - advancedPlayers.size();
        for (int i = 0; i < remainingSlots; i++) {
            advancedPlayers.add(tiedPlayers.get(i).getPlayerId());
        }
        System.out.println("Advanced players after adding remaining slots: " + advancedPlayers);

        return advancedPlayers;
    }

    /**
     * Calculates the opponent difficulty scores for tied players.
     *
     * @param tiedPlayers  the list of SwissStanding objects representing tied
     *                     players
     * @param tournamentId the ID of the tournament
     * @return a map of player IDs to their opponent difficulty scores
     */
    private Map<Long, Double> calculateOpponentDifficultyScores(List<SwissStanding> tiedPlayers, Long tournamentId) {
        Map<Long, Double> difficultyScores = new HashMap<>();

        for (SwissStanding playerStanding : tiedPlayers) {
            Long playerId = playerStanding.getPlayerId();

            // Get the player's Elo rating
            double playerElo = getEloRating(playerId);

            // Get all matches for the player in the tournament
            List<Match> matches = matchRepository.findByPlayerIdInTournament(playerId, tournamentId);

            // Calculate the difficulty score with weights based on win/loss
            double difficultyScore = matches.stream()
                    .mapToDouble(match -> {
                        // Get the opponent's Elo rating
                        double opponentElo = getOpponentElo(match, playerId);

                        // Check if the player won the match
                        boolean isWin = match.getWinnerId().equals(playerId);

                        // Set the base weight based on whether the player won the match
                        double baseWeight = isWin ? 1.0 : 0.7;

                        // Adjust the weight based on the opponent's Elo rating
                        // If the opponent's Elo rating is higher than the player's Elo rating, increase
                        // the weight
                        // Encourages beating or closely competing with stronger players without overly
                        // penalizing losses
                        double weight = baseWeight * (opponentElo > playerElo ? 1.2 : 1.0);

                        return opponentElo * weight;
                    })
                    .sum();

            difficultyScores.put(playerId, difficultyScore);
        }
        return difficultyScores;
    }

    /**
     * Retrieves the Elo rating of the opponent for a given match.
     *
     * @param match    the match to find the opponent in
     * @param playerId the ID of the player to find the opponent for
     * @return the Elo rating of the opponent, or 0.0 if the opponent details are
     *         not found
     */
    private double getOpponentElo(Match match, Long playerId) {
        // Determine the opponent's ID for the given match
        Long opponentId = match.getPlayer1Id().equals(playerId) ? match.getPlayer2Id() : match.getPlayer1Id();
        // Get the opponent's Elo rating
        return getEloRating(opponentId);
    }

    /**
     * Retrieves the Elo rating of a player based on their ID.
     *
     * @param playerId the ID of the player to retrieve the Elo rating for
     * @return the Elo rating of the player, or 0.0 if the player details are not
     *         found
     */
    private double getEloRating(Long playerId) {
        // Get the player's details from the player service
        PlayerDetailsDTO playerDetails = restTemplate.getForObject(playerServiceUrl + "/api/player/" + playerId,
                PlayerDetailsDTO.class);
        return playerDetails != null ? playerDetails.getEloRating() : 0.0;
    }

    /**
     * Advances a knockout match to the next round, progressing through the bracket
     * structure
     * until the final winner is determined.
     *
     * @param match    the completed match
     * @param winnerId the ID of the match winner
     * @return a message indicating the progress of the knockout tournament
     */
    private String advanceKnockoutMatch(Match match, Long winnerId) {
        // Check if there is a next match
        if (match.getNextMatchId() != null) {
            // Get the matches for the current round
            List<Match> roundMatches = getRoundMatches(match.getTournamentId(), match.getRoundType().getId());

            // Check if the round is completed
            if (isRoundCompleted(roundMatches)) {
                Match nextMatch = getNextMatch(match.getNextMatchId());

                // Update the current round for the tournament
                updateCurrentRoundForTournament(match.getTournamentId(), nextMatch.getRoundType().getId());

                // Assign matches for the next round
                assignNextRoundMatches(getRoundMatches(match.getTournamentId(), match.getRoundType().getId()));

                return "Tournament has advanced to the next round";
            }
            return "Winner advanced to the next round";
        } else {
            // Update the tournament winner
            updateTournamentWinner(match.getTournamentId(), winnerId);
            return "Tournament completed";
        }
    }

    /**
     * Checks if all matches in a given round are completed.
     *
     * @param roundMatches the list of matches in the round
     * @return true if all matches are completed, false otherwise
     */
    private boolean isRoundCompleted(List<Match> roundMatches) {
        return roundMatches.stream().allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);
    }

    /**
     * Retrieves all matches for a specific tournament and round type.
     *
     * @param tournamentId the tournament ID
     * @param roundTypeId  the round type ID
     * @return a list of Match objects for the specified tournament and round type
     */
    private List<Match> getRoundMatches(Long tournamentId, Long roundTypeId) {
        return matchRepository.findByTournamentIdAndRoundTypeId(tournamentId, roundTypeId);
    }

    /**
     * Retrieves a single match by its ID.
     *
     * @param nextMatchId the ID of the next match
     * @return the Match object with the specified ID
     */
    private Match getNextMatch(Long nextMatchId) {
        return matchRepository.findById(nextMatchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Next match not found"));
    }

    /**
     * Updates the tournament winner by calling the external tournament service.
     *
     * @param tournamentId the ID of the tournament
     * @param winnerId     the ID of the tournament winner
     */
    private void updateTournamentWinner(Long tournamentId, Long winnerId) {
        // Update the tournament winner by calling the tournament service
        String updateTournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/winner/" + winnerId;
        restTemplate.put(updateTournamentUrl, null);
    }

    /**
     * Assigns the next round matches for the completed matches in a round.
     *
     * @param roundMatches the list of completed matches in the round
     */
    private void assignNextRoundMatches(List<Match> roundMatches) {
        // Iterate through the completed matches
        for (Match completedMatch : roundMatches) {
            // Get the next match ID
            Long nextMatchId = completedMatch.getNextMatchId();
            if (nextMatchId != null) {
                // Get the next match
                Match advanceMatch = matchRepository.findById(nextMatchId)
                        .orElseThrow(() -> new RuntimeException(
                                "Next match not found for match ID: " + nextMatchId));

                // Set the winner ID to the next match
                if (advanceMatch.getPlayer1Id() == null) {
                    advanceMatch.setPlayer1Id(completedMatch.getWinnerId());
                } else if (advanceMatch.getPlayer2Id() == null) {
                    advanceMatch.setPlayer2Id(completedMatch.getWinnerId());
                }
                matchRepository.save(advanceMatch);
            }
        }
    }

    /**
     * Retrieves the top 5 most recent matches played by a player, either as player1
     * or player2.
     *
     * @param playerId the ID of the player
     * @return a list of MatchDTO objects representing recent matches
     */
    public List<MatchDTO> getRecentMatchesByPlayerId(Long playerId) {

        List<Match> matches = matchRepository.findTop5ByPlayer1IdOrPlayer2IdOrderByUpdatedAtDesc(playerId, playerId);

        // Convert the matches to MatchDTO objects
        List<MatchDTO> matchDTOs = matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());

        return matchDTOs;
    }

    /**
     * Converts a Match entity to a MatchDTO object.
     *
     * @param match the Match entity to convert
     * @return the MatchDTO object
     */
    private MatchDTO convertMatchToMatchDTO(Match match) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());

        // Set player details
        matchDTO.setPlayer1(getPlayerDetailsOrNull(match.getPlayer1Id()));
        matchDTO.setPlayer2(getPlayerDetailsOrNull(match.getPlayer2Id()));

        // Set winner and loser IDs
        setWinnerAndLoserIds(match, matchDTO);

        // Set additional match properties
        matchDTO.setSwissRoundNumber(match.getSwissRoundNumber());
        matchDTO.setRoundType(match.getRoundType());
        matchDTO.setGameType(match.getGameType());
        matchDTO.setDate(match.getUpdatedAt());

        // Set tournament details
        matchDTO.setTournament(getTournamentDetails(match.getTournamentId()));

        return matchDTO;
    }

    /**
     * Retrieves player details if the player ID is not null, otherwise returns
     * null.
     *
     * @param playerId the ID of the player
     * @return the PlayerDetailsDTO object if the player ID is not null, otherwise
     *         null
     */
    private PlayerDetailsDTO getPlayerDetailsOrNull(Long playerId) {
        // Get the player details from the player service if the player ID is not null
        return (playerId != null) ? getPlayerDetails(playerId) : null;
    }

    /**
     * Sets the winner and loser IDs for a MatchDTO object.
     *
     * @param match    the Match entity
     * @param matchDTO the MatchDTO object to set the IDs on
     */
    private void setWinnerAndLoserIds(Match match, MatchDTO matchDTO) {
        matchDTO.setWinnerId(match.getWinnerId());
        matchDTO.setLoserId(match.getLoserId());
    }

    /**
     * Retrieves tournament details by calling the external tournament service.
     *
     * @param tournamentId the ID of the tournament
     * @return the TournamentDTO object for the specified tournament
     */
    private TournamentDTO getTournamentDetails(Long tournamentId) {
        // Get the tournament details from the tournament service
        String tournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId;
        return restTemplate.getForObject(tournamentUrl, TournamentDTO.class);
    }

    /**
     * Retrieves player details by calling the external player service.
     *
     * @param playerId the ID of the player
     * @return the PlayerDetailsDTO object for the specified player
     */
    private PlayerDetailsDTO getPlayerDetails(long playerId) {
        // Get the player details from the player service
        String playerUrl = playerServiceUrl + "/api/player/" + playerId;
        return restTemplate.getForObject(playerUrl, PlayerDetailsDTO.class);
    }

    /**
     * Creates a Match entity with provided details.
     *
     * @param tournamentId,     Long player1Id, Long player2Id, Long winnerId, Long
     *                          loserId,
     * @param swissRoundNumber, RoundType roundType, Long gameTypeId, Long
     *                          nextMatchId,
     * @param status
     * @return the created Match entity
     */
    private Match createMatch(Long tournamentId, Long player1Id, Long player2Id, Long winnerId, Long loserId,
            Integer swissRoundNumber, RoundType roundType, Long gameTypeId, Long nextMatchId,
            MatchStatus status) {
        Match match = new Match();
        match.setTournamentId(tournamentId);
        match.setPlayer1Id(player1Id);
        match.setPlayer2Id(player2Id);
        match.setWinnerId(winnerId);
        match.setLoserId(loserId);
        match.setSwissRoundNumber(swissRoundNumber);
        match.setRoundType(roundType);
        match.setGameType(gameTypeRepository.findById(gameTypeId)
                .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
        match.setNextMatchId(nextMatchId);
        match.setStatus(status);

        return match;
    }

    /**
     * Checks if two players have played against each other before.
     *
     * @param playerMatchHistory the map of player IDs to their match history
     * @param player1Id          the ID of the first player
     * @param player2Id          the ID of the second player
     * @return true if the players have played against each other, false otherwise
     */
    private boolean hasPlayedBefore(Map<Long, Set<Long>> playerMatchHistory, Long player1Id, Long player2Id) {
        return playerMatchHistory.getOrDefault(player1Id, new HashSet<>()).contains(player2Id);
    }
}
