package com.chess.tms.elo_service.eloService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;
import com.chess.tms.elo_service.service.EloService;
import com.chess.tms.elo_service.dto.DTOUtil;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.enums.Reason;


@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class EloServiceTest {
    
    @Mock
    private EloRepository eloRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EloService eloService;

    // @BeforeEach
    // void setUp() {
    //     MockitoAnnotations.openMocks(this);
    //     mockMvc = MockMvcBuilders.standaloneSetup(eloService)
    //                 .setControllerAdvice(new GlobalExceptionHandler())
    //                 .build();
    // // insert dummy data into repository
    // }

    @Test
    public void testFindAllByEloHistory() {
        List<EloHistory> list = new ArrayList<>();
        LocalDateTime t1 = LocalDateTime.now();
        LocalDateTime t2 = LocalDateTime.now();

        list.add(new EloHistory(1, 12345, 1315, 1315, Reason.WIN, t1));
        list.add(new EloHistory(2, 13333, 1330, 1315, Reason.LOSS, t2));

        List<EloResponseDTO> expected = DTOUtil.convertEntriesToResponseDTOs(list);

        when(eloRepository.findAll())
            .thenReturn(list);

        List<EloResponseDTO> ans = eloService.findAllByEloHistory();
        assertEquals(expected, ans);
        verify(eloRepository, times(1)).findAll();
        
    }
    
}
