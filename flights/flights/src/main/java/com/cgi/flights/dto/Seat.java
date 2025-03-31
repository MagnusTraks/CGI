package com.cgi.flights.dto;

import com.cgi.flights.model.SeatType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    private String seatNumber;
    private int row;
    private char seatChar;
    private SeatType type;
    private boolean hasExtraLegroom;
    private boolean isNearExit;
    private boolean occupied;
}