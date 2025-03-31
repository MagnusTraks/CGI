package com.cgi.flights.controller;

import com.cgi.flights.dto.Seat;
import com.cgi.flights.dto.SeatPreferences;
import com.cgi.flights.model.Aircraft;
import com.cgi.flights.model.Flight;
import com.cgi.flights.model.SeatType;
import com.cgi.flights.service.FlightService;
import com.cgi.flights.service.SeatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);

    private final FlightService flightService;
    private final SeatService seatService;

    @Autowired
    public WebController(FlightService flightService, SeatService seatService) {
        this.flightService = flightService;
        this.seatService = seatService;
    }


    @GetMapping("/")
    public String showFlightSearchPage(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer maxDurationHours,
            Model model) {

        log.info("Kuvatakse lendude otsimise leht filtritega: dest={}, kuup={}, hind={}, kestus_h={}", destination, date, maxPrice, maxDurationHours);

        Integer maxDurationMinutes = null;
        if (maxDurationHours != null && maxDurationHours > 0) {
            maxDurationMinutes = maxDurationHours * 60;
        }

        List<Flight> flights = flightService.searchFlights(destination, date, maxPrice, maxDurationMinutes);

        model.addAttribute("flights", flights);
        model.addAttribute("destination", destination);
        model.addAttribute("date", date);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("maxDurationHours", maxDurationHours);

        return "flights";
    }

    // == Istekoha Plaani ja Soovitamise Leht ==

    @GetMapping("/flight/{flightId}/seats")
    public String showSeatMapPage(@PathVariable Long flightId, Model model) {
        log.info("Kuvatakse istekoha plaan lennule {}", flightId);
        try {
            Flight flight = flightService.getFlightById(flightId);
            Aircraft aircraft = flight.getAircraft();
            List<Seat> seatMapList = seatService.getSeatMap(flightId);

            Map<Integer, Map<Character, Seat>> seatMapByRowAndChar = seatMapList.stream()
                    .sorted(Comparator.comparing(Seat::getRow).thenComparing(Seat::getSeatChar))
                    .collect(Collectors.groupingBy(
                            Seat::getRow,
                            LinkedHashMap::new,
                            Collectors.toMap(
                                    Seat::getSeatChar,
                                    seat -> seat,
                                    (seat1, seat2) -> seat1,
                                    LinkedHashMap::new
                            )
                    ));

            List<Character> seatChars = seatService.getSeatChars(aircraft);
            Map<Integer, Integer> aisleIndicesMap = seatService.getAisleIndices(aircraft.getSeatConfiguration());
            List<Integer> aisleAfterIndices = aisleIndicesMap.values().stream().toList();

            model.addAttribute("flight", flight);
            model.addAttribute("seatMapByRow", seatMapByRowAndChar);
            model.addAttribute("aircraft", aircraft);
            model.addAttribute("seatChars", seatChars);
            model.addAttribute("aisleAfterIndices", aisleAfterIndices);
            model.addAttribute("seatPreferences", new SeatPreferences());
            model.addAttribute("seatTypes", SeatType.values());
            model.addAttribute("recommendedSeatNumbers", Collections.emptyList());
            model.addAttribute("recommendationAttempted", false);

        } catch (RuntimeException e) {
            log.error("Lendu {} ei leitud istekoha plaani kuvamisel", flightId, e);
            return "redirect:/?error=flightNotFound";
        }
        return "seatmap";
    }

    // == Istekohtade Soovituste Käitlemine (POST päring vormilt) ==

    @PostMapping("/flight/{flightId}/recommend")
    public String recommendSeatsAndShowMap(
            @PathVariable Long flightId,
            @ModelAttribute SeatPreferences preferences,
            Model model) {

        log.info("Käideldakse istekoha soovituse päringut lennule {} eelistustega: {}", flightId, preferences);

        List<Seat> recommendedSeats = seatService.recommendSeats(flightId, preferences);
        List<String> recommendedSeatNumbers = recommendedSeats.stream()
                .map(Seat::getSeatNumber)
                .toList();

        try {
            Flight flight = flightService.getFlightById(flightId);
            Aircraft aircraft = flight.getAircraft();
            List<Seat> seatMapList = seatService.getSeatMap(flightId);
            Map<Integer, Map<Character, Seat>> seatMapByRowAndChar = seatMapList.stream()
                    .sorted(Comparator.comparing(Seat::getRow).thenComparing(Seat::getSeatChar))
                    .collect(Collectors.groupingBy(
                            Seat::getRow,
                            LinkedHashMap::new,
                            Collectors.toMap(
                                    Seat::getSeatChar,
                                    seat -> seat,
                                    (seat1, seat2) -> seat1,
                                    LinkedHashMap::new
                            )
                    ));

            List<Character> seatChars = seatService.getSeatChars(aircraft);
            Map<Integer, Integer> aisleIndicesMap = seatService.getAisleIndices(aircraft.getSeatConfiguration());
            List<Integer> aisleAfterIndices = aisleIndicesMap.values().stream().toList();

            model.addAttribute("flight", flight);
            model.addAttribute("seatMapByRow", seatMapByRowAndChar);
            model.addAttribute("aircraft", aircraft);
            model.addAttribute("seatChars", seatChars);
            model.addAttribute("aisleAfterIndices", aisleAfterIndices);
            model.addAttribute("seatPreferences", preferences);
            model.addAttribute("seatTypes", SeatType.values());
            model.addAttribute("recommendedSeatNumbers", recommendedSeatNumbers);
            model.addAttribute("recommendationAttempted", true);

        } catch (RuntimeException e) {
            log.error("Lendu {} ei leitud soovituste käsitlemisel", flightId, e);
            return "redirect:/?error=flightNotFound";
        }

        return "seatmap";
    }
}