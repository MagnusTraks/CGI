package com.cgi.flights.dto;

import com.cgi.flights.model.SeatType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatPreferences {

    private int numberOfSeats = 1;

    private SeatType preferredSeatType;

    private boolean requireExtraLegroom = false;
    private boolean preferNearExit = false;

    private boolean requireAdjacentSeats = true;
}