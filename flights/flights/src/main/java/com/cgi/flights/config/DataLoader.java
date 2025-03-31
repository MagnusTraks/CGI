package com.cgi.flights.config;

import com.cgi.flights.model.Aircraft;
import com.cgi.flights.model.Flight;
import com.cgi.flights.repository.AircraftRepository;
import com.cgi.flights.repository.FlightRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DataLoader {

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;

    public DataLoader(FlightRepository flightRepository, AircraftRepository aircraftRepository) {
        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        System.out.println("Starting data loading...");

        // --- 1. SAMM: Lae või loo Lennukid ---
        Aircraft atr72, a220, crj900, a320;

        if (aircraftRepository.count() == 0) {
            System.out.println("Aircraft data not found. Creating new aircraft...");

            Aircraft newAtr72 = new Aircraft(null, "ATR 72", 18, "AC-DF",
                    new HashSet<>(Arrays.asList(1, 2)), new HashSet<>(Arrays.asList(1, 18)));
            Aircraft newA220 = new Aircraft(null, "Airbus A220", 25, "ABC-DE",
                    new HashSet<>(Arrays.asList(10, 11)), new HashSet<>(Arrays.asList(10, 11)));
            Aircraft newCrj900 = new Aircraft(null, "CRJ 900", 22, "AC-DF",
                    new HashSet<>(Arrays.asList(8)), new HashSet<>(Arrays.asList(8)));
            Aircraft newA320 = new Aircraft(null, "Airbus A320", 30, "ABC-DEF",
                    new HashSet<>(Arrays.asList(12, 14)), new HashSet<>(Arrays.asList(12, 14)));

            List<Aircraft> savedAircraft = aircraftRepository.saveAll(Arrays.asList(newAtr72, newA220, newCrj900, newA320));

            Map<String, Aircraft> savedAircraftMap = savedAircraft.stream()
                    .collect(Collectors.toMap(Aircraft::getModel, Function.identity()));

            atr72 = savedAircraftMap.get("ATR 72");
            a220 = savedAircraftMap.get("Airbus A220");
            crj900 = savedAircraftMap.get("CRJ 900");
            a320 = savedAircraftMap.get("Airbus A320");

            System.out.println("Aircraft data created and saved.");

        } else {
            System.out.println("Aircraft data already exists. Fetching existing aircraft...");
            List<Aircraft> existingAircraft = aircraftRepository.findAll();
            Map<String, Aircraft> aircraftMap = existingAircraft.stream()
                    .collect(Collectors.toMap(Aircraft::getModel, Function.identity()));

            atr72 = aircraftMap.get("ATR 72");
            a220 = aircraftMap.get("Airbus A220");
            crj900 = aircraftMap.get("CRJ 900");
            a320 = aircraftMap.get("Airbus A320");

            System.out.println("Existing aircraft fetched.");
        }

        if (atr72 == null || a220 == null || crj900 == null || a320 == null) {
            throw new IllegalStateException("FATAL: Could not load or create all required aircraft models. Cannot proceed with loading flights.");
        }

        // --- 2. SAMM: Lae Lennud (ainult kui neid veel pole) ---
        if (flightRepository.count() == 0) {
            System.out.println("Flight data not found. Creating new flights...");

            Flight f1 = new Flight(null, "AY1072", "TLL", "HEL",
                    LocalDateTime.of(2024, 9, 15, 10, 30),
                    LocalDateTime.of(2024, 9, 15, 11, 05),
                    new BigDecimal("85.50"), atr72);

            Flight f2 = new Flight(null, "BT362", "TLL", "RIX",
                    LocalDateTime.of(2024, 9, 15, 14, 00),
                    LocalDateTime.of(2024, 9, 15, 14, 50),
                    new BigDecimal("65.00"), a220);

            Flight f3 = new Flight(null, "SK1785", "TLL", "ARN",
                    LocalDateTime.of(2024, 9, 16, 9, 10),
                    LocalDateTime.of(2024, 9, 16, 9, 50),
                    new BigDecimal("110.99"), crj900);

            Flight f4 = new Flight(null, "LH881", "TLL", "FRA",
                    LocalDateTime.of(2024, 9, 16, 13, 45),
                    LocalDateTime.of(2024, 9, 16, 15, 20),
                    new BigDecimal("175.80"), a320);

            Flight f5 = new Flight(null, "AY1074", "TLL", "HEL",
                    LocalDateTime.of(2024, 9, 16, 18, 15),
                    LocalDateTime.of(2024, 9, 16, 18, 50),
                    new BigDecimal("95.00"), atr72);

            flightRepository.saveAll(Arrays.asList(f1, f2, f3, f4, f5));
            System.out.println("Flight data created and saved.");
        } else {
            System.out.println("Flight data already exists. Skipping flight creation.");
        }

        System.out.println("Data loading finished.");
    }
}