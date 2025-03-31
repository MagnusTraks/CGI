package com.cgi.flights.controller;

import com.cgi.flights.dto.Seat;
import com.cgi.flights.dto.SeatPreferences;
import com.cgi.flights.service.FlightService;
import com.cgi.flights.service.SeatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;
    private final SeatService seatService;

    @Autowired
    public FlightController(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }

    /**
     * POST /api/flights/{flightId}/recommend-seats
     * Soovitab istekohti vastavalt esitatud eelistustele.
     *
     * @param flightId    Lennu ID.
     * @param preferences Istekoha eelistused
     * @return Listi soovitatud istekohtadest
     */
    @PostMapping("/{flightId}/recommend-seats")
    public ResponseEntity<List<Seat>> recommendSeats(
            @PathVariable Long flightId,
            @RequestBody SeatPreferences preferences) {

        if (preferences == null || preferences.getNumberOfSeats() <= 0) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        try {
            List<Seat> recommendedSeats = seatService.recommendSeats(flightId, preferences);
            if (recommendedSeats.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            } else {
                return ResponseEntity.ok(recommendedSeats);
            }
        } catch (RuntimeException e) {
            System.err.println("Error recommending seats for flight " + flightId + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Unexpected error recommending seats for flight " + flightId + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}