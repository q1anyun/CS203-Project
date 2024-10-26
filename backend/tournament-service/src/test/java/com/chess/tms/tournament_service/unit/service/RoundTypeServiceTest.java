package com.chess.tms.tournament_service.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.chess.tms.tournament_service.repository.RoundTypeRepository;
import com.chess.tms.tournament_service.service.RoundTypeService;

@ExtendWith(MockitoExtension.class)
public class RoundTypeServiceTest {

    @Mock
    private RoundTypeRepository roundTypeRepository;

    @InjectMocks
    private RoundTypeService roundTypeService;

    private List<Integer> numberOfPlayersChoices;

    @BeforeEach
    void setup() {
        numberOfPlayersChoices = Arrays.asList(2, 4, 8, 16, 32, 64);
    }

    @Test
    void getChoicesForNumberOfPlayers_Valid_ReturnChoices() {
        when(roundTypeRepository.findDistinctNumberOfPlayers()).thenReturn(numberOfPlayersChoices);

        List<Integer> result = roundTypeService.getChoicesForNumberOfPlayers();

        assertEquals(6, result.size());
        assertEquals(2, result.get(0));
        assertEquals(16, result.get(3));
    }
}