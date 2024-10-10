package com.chess.tms.elo_service.exception.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EloHistoryChartDTO {
    private int elo;
    private LocalDate date;
}
