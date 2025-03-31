package com.cgi.flights.service;

import com.cgi.flights.model.Flight;
import com.cgi.flights.repository.FlightRepository;
import com.cgi.flights.repository.specification.FlightSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    @Autowired
    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    /**
     * Tagastab kõik lennud.
     * @return List lendudest
     */
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    /**
     * Otsib lende vastavalt kriteeriumitele.
     *
     * @param destination Sihtkoht
     * @param date Kuupäev
     * @param maxPrice Maksimaalne hind
     * @param maxDurationMinutes Maksimaalne kestvus minutites
     * @return Filtreeritud lendude nimekiri.
     */
    public List<Flight> searchFlights(String destination, LocalDate date, BigDecimal maxPrice, Integer maxDurationMinutes) { // LISA maxDurationMinutes
        Specification<Flight> spec = FlightSpecification.getFlightsByCriteria(destination, date, maxPrice, maxDurationMinutes); // EDASI maxDurationMinutes

        List<Flight> results = flightRepository.findAll(spec);

        return results;
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + id));
    }

}