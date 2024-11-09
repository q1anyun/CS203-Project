package com.chess.tms.elo_service.unit.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;
import com.chess.tms.elo_service.service.EloService;
import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.dto.WinLossUpdateDTO;
import com.chess.tms.elo_service.enums.Reason;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;


@RunWith(MockitoJUnitRunner.class)
public class EloServiceTest {

    private AutoCloseable openMocks; //used to initialise and close mocks after each test

    @Value("${players.service.url}")
    private String playersServiceUrl; //defined in application-test.properties
    
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EloRepository eloRepository;

    @InjectMocks
    private EloService eloService;

    List<EloHistory> list = new ArrayList<>();



    @BeforeEach
    public void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        list = new ArrayList<>();
    }

    @AfterEach
    public void tearDown() throws Exception{
       openMocks.close();
    }

    @Test
    public void findAllByEloHistory_Valid_Success() {

        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = LocalDateTime.now();

        list.add(new EloHistory(1, 12345, 1315, 1315, Reason.WIN, t1));
        list.add(new EloHistory(2, 13333, 1330, 1315, Reason.LOSS, t2));

        List<EloResponseDTO> expected = convertEntriesToResponseDTOs(list);

        when(eloRepository.findAll())
            .thenReturn(list);

        List<EloResponseDTO> ans = eloService.findAllByEloHistory();
        assertEquals(expected, ans);
        verify(eloRepository, times(1)).findAll();
        
    }

    @Test
    public void findEloHistoryByPlayerId_Valid_Success() {

       LocalDateTime t1 = LocalDateTime.now();

       list.add(new EloHistory(1, 12345, 1315, 1315, Reason.WIN, t1));
        List<EloResponseDTO> expected = convertEntriesToResponseDTOs(list);

       when(eloRepository.findByPlayerId(1))
        .thenReturn(Optional.of(list));

        List<EloResponseDTO> ans = eloService.findEloHistoryByPlayerId(1);
        assertEquals(expected, ans);

        verify(eloRepository, times(1)).findByPlayerId(1);
    }

    @Test
    public void findEloHistoryByPlayerId_InvalidId_Exception() {
        String expectedExceptionMessage = "Player with player id 1 has no history.";
        String exceptionMsg = "";

        when(eloRepository.findByPlayerId(1))
        .thenReturn(Optional.empty());

        try {
            eloService.findEloHistoryByPlayerId(1);
        } catch (PlayerHistoryNotFoundException e) {
            exceptionMsg = e.getMessage();
        }

        assertEquals(expectedExceptionMessage, exceptionMsg);
        verify(eloRepository, times(1)).findByPlayerId(1);
    }

    @Test 
    public void deleteByPlayerId_Success(){
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = LocalDateTime.now();

        List<EloHistory> list1 = new ArrayList<>();
        List<EloHistory> list2 = new ArrayList<>();
        list1.add(new EloHistory(1, 12345, 1315, 1315, Reason.WIN, t1));
        list2.add(new EloHistory(2, 13333, 1330, 1315, Reason.LOSS, t2));

        List<EloResponseDTO> expected = convertEntriesToResponseDTOs(list1);

        Mockito.doNothing().when(eloRepository).deleteByPlayerId(1);
        when(eloRepository.findByPlayerId(1))
            .thenReturn(Optional.of(list1));
            
        List<EloResponseDTO> ans = eloService.deleteByPlayerId(1);  

        assertEquals(expected, ans);
        verify(eloRepository, times(1)).deleteByPlayerId(1);
    }

    @Test 
    public void deleteByPlayerId_InvalidId_Exception(){
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = LocalDateTime.now();
        String exceptionMsg = "";
        String expectedExceptionMsg = "Player with player id 1 has no history.";
        list.add(new EloHistory(1, 12345, 1315, 1315, Reason.WIN, t1));
        list.add(new EloHistory(2, 13333, 1330, 1315, Reason.LOSS, t2));


        //Mockito.doNothing().when(eloRepository).deleteByPlayerId(1);
        when(eloRepository.findByPlayerId(1))
            .thenReturn(Optional.empty());
            
            try {
                eloService.findEloHistoryByPlayerId(1);
            } catch (PlayerHistoryNotFoundException e) {
                exceptionMsg = e.getMessage();
            }
    
            assertEquals(expectedExceptionMsg, exceptionMsg);
            
            verify(eloRepository, times(1)).findByPlayerId(1);
    }

    @Test
    public void saveEloHistory_Success(){
        LocalDateTime t1 = LocalDateTime.of(LocalDate.now(), LocalTime.of(0,0));

        EloHistory eloHistory = new EloHistory(0, 12345, 1215, 1315, Reason.WIN, t1);
        EloResponseDTO expected = convertEntryToResponseDTO(eloHistory);

        EloHistoryRequestDTO reqDto = new EloHistoryRequestDTO();
        reqDto.setPlayerId(12345);
        reqDto.setNewElo(1315);
        reqDto.setChangeReason(Reason.WIN);

        when(eloRepository.save(eloHistory))
            .thenReturn((eloHistory));
        
        EloResponseDTO actual = eloService.saveEloHistory(1215, reqDto, t1);
        assertEquals(expected.getOldElo(), actual.getOldElo());
        assertEquals(expected.getChangeReason(), actual.getChangeReason());
        assertEquals(expected.getPlayerId(), actual.getPlayerId());
            
        verify(eloRepository, times(1)).save(eloHistory);
    }

    @Test 
    public void findByPlayerIdAndChangeReason_Valid_Success() {
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t3 = LocalDateTime.now();

        List<EloHistory> list1 = new ArrayList<>();
        list1.add(new EloHistory(1, 12345, 1215, 1315, Reason.WIN, t3));
        list1.add(new EloHistory(1, 12345, 1215, 1315, Reason.WIN, t1));
        List<EloResponseDTO> expected = convertEntriesToResponseDTOs(list1);

        when(eloRepository.findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(12345, Reason.WIN))
            .thenReturn(list1);
        List<EloResponseDTO> actual = eloService.findByPlayerIdAndChangeReason(12345, "win");
        assertEquals(expected, actual);
        verify(eloRepository, times(1)).findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(12345, Reason.WIN);
    }

    @Test
    public void findPlayerEloHistoryForChart_MoreThanOrEqualTo5Entries_Success() {
        LocalDate[] dates = new LocalDate[10];
        List<EloHistoryChartDTO> expected = new ArrayList<>();
        List<EloHistory> list = new ArrayList<>();
        int elo = 1300;
        
        // set days for LocalDate
        for(int day = 1; day <= 5; day++) {
            dates[day] = LocalDate.of(2024, 11, day);
        }

        // set EloHistory for each day
        for (int day = 1; day <= 5; day++) {
            expected.add(new EloHistoryChartDTO(elo - 10, dates[day]));
            list.add(new EloHistory(day, 12345, elo, elo - 10, Reason.WIN, LocalDateTime.of(2024, 11, day, 11, 0)));
            elo -= 10;
        }

        
        when(eloRepository.findLatestEloHistoryByPlayerId(12345L))
            .thenReturn(list);

        EloHistoryChartDTO[] actual = eloService.findPlayerEloHistoryForChart(12345L);
        
        assertArrayEquals(expected.toArray(new EloHistoryChartDTO[5]), actual);
        verify(eloRepository, times(1)).findLatestEloHistoryByPlayerId(12345L);
    }

    @Test
    public void findPlayerEloHistoryForChart_LessThan5Entries_Success() {
        LocalDate[] dates = new LocalDate[10];
        List<EloHistoryChartDTO> expected = new ArrayList<>();
        List<EloHistory> list = new ArrayList<>();
        int elo = 1300;
        
        // set days for LocalDate
        for(int day = 1; day <= 3; day++) {
            dates[day] = LocalDate.of(2024, 11, day);
        }

        // set EloHistory for each day
        for (int day = 1; day <= 3; day++) {
            expected.add(new EloHistoryChartDTO(elo - 10, dates[day]));
            list.add(new EloHistory(day, 12345, elo, elo - 10, Reason.WIN, LocalDateTime.of(2024, 11, day, 11, 0)));
            elo -= 10;
        }

        when(eloRepository.findLatestEloHistoryByPlayerId(12345L))
            .thenReturn(list);

        EloHistoryChartDTO[] actual = eloService.findPlayerEloHistoryForChart(12345L);
        
        assertArrayEquals(expected.toArray(new EloHistoryChartDTO[3]), actual);
        verify(eloRepository, times(1)).findLatestEloHistoryByPlayerId(12345L);
    }

    /**
     * This function calculates the new elo for a player based on the current elo of the player and the elo change.
     * The elochange is calculated based on the probability of winning against the opponent, as well as the elo difference
     * between the two players.
     * @param winnerElo - the current elo of the winner
     * @param loserElo - the current elo of the loser
     * @return an array containing the new elo for the winner and the loser
     */
    public int[] calculateEloChange(int winnerElo, int loserElo) {
        double winnerWinProb = 1.0 / (1.0 + (Math.pow(10, (loserElo - winnerElo) / 400)));
        double loserWinProb = 1.0 / (1.0 + (Math.pow(10, (winnerElo - loserElo) / 400)));
        int maxEloChange = 32;

        int[] changedElo = new int[2];

        changedElo[0] = (int) (winnerElo + (1.0 - winnerWinProb) * maxEloChange);
        changedElo[1] = (int) (loserElo + (0.0 - loserWinProb) * maxEloChange);

        //handles the case where the changedElo is the same as the currentElo of player
        if (changedElo[0] == winnerElo) changedElo[0] += 1;
        if (changedElo[1] == loserElo) changedElo[1] -= 1;

        return changedElo;
    }


    @Test
    public void updatePlayersEloAfterCompletedMatch_Valid_Success() {
        long winnerId = 12345;
        long loserId = 13333;
        int currWinElo = 1500;
        int currLoseElo = 1455;
        int[] newElo = calculateEloChange(currWinElo, currLoseElo);
        int newWinElo = newElo[0];
        int newLoseElo = newElo[1];

        // mock response for GET requests to playerService
        when(restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + winnerId,
        Integer.class)).thenReturn(currWinElo);
        when(restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + loserId,
        Integer.class)).thenReturn(currLoseElo);

        WinLossUpdateDTO winDto = new WinLossUpdateDTO(winnerId, newWinElo, true);
        WinLossUpdateDTO lossDto = new WinLossUpdateDTO(loserId, newLoseElo, false);
        String updateServiceUrl = playersServiceUrl + "/api/player/winLossElo" ;
        
        Mockito.doNothing().when(restTemplate).put(updateServiceUrl, winDto);
        Mockito.doNothing().when(restTemplate).put(updateServiceUrl, lossDto);

        eloService.updatePlayersEloAfterCompletedMatch(new MatchEloRequestDTO(winnerId, loserId));

        verify(restTemplate, times(1)).getForObject(playersServiceUrl + "/api/player/elo/" + winnerId,
        Integer.class);
        verify(restTemplate, times(1)).getForObject(playersServiceUrl + "/api/player/elo/" + loserId,
        Integer.class);
        verify(restTemplate, times(1)).put(updateServiceUrl, winDto);
        verify(restTemplate, times(1)).put(updateServiceUrl, winDto);
    }


    // Helper functions to convert entries to DTOs and vice versa
    public EloResponseDTO convertEntryToResponseDTO(EloHistory eloHistory) {
        EloResponseDTO eloResponseDTO = new EloResponseDTO();
        
        eloResponseDTO.setPlayerId(eloHistory.getPlayerId());
        eloResponseDTO.setOldElo(eloHistory.getOldElo());
        eloResponseDTO.setNewElo(eloHistory.getNewElo());
        eloResponseDTO.setCreatedAt(eloHistory.getCreatedAt());
        eloResponseDTO.setChangeReason(eloHistory.getChangeReason());
        return eloResponseDTO;
    }

    public List<EloResponseDTO> convertEntriesToResponseDTOs(List<EloHistory> list) {
        List<EloResponseDTO> responses = new ArrayList<>();
        for(EloHistory eloHistory : list) {
            responses.add(convertEntryToResponseDTO(eloHistory));
        }

        return responses;
    }


}
