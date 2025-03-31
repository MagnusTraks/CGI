package com.cgi.flights.service;

import com.cgi.flights.dto.Seat;
import com.cgi.flights.dto.SeatPreferences;
import com.cgi.flights.model.Aircraft;
import com.cgi.flights.model.Flight;
import com.cgi.flights.model.SeatType;
import com.cgi.flights.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class SeatService {


    private static final Logger log = LoggerFactory.getLogger(SeatService.class);

    private final FlightRepository flightRepository;
    private final Map<Long, Set<String>> occupiedSeatsCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @Autowired
    public SeatService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    /**
     * Genereerib istekoha plaani konkreetse lennu jaoks, sh juhuslikult määratud hõivatud kohad.
     * @param flightId Lennu ID.
     * @return List istmetest.
     * @throws RuntimeException kui lendu ei leita.
     */
    public List<Seat> getSeatMap(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found with id: " + flightId));
        Aircraft aircraft = flight.getAircraft();
        Set<String> occupiedSeats = getOrGenerateOccupiedSeats(flightId, aircraft);

        List<Seat> seatMap = new ArrayList<>();
        List<Character> seatChars = getSeatChars(aircraft);
        Map<Integer, Integer> aisleIndicesMap = getAisleIndices(aircraft.getSeatConfiguration());
        log.debug("Generating seat map for Aircraft: {}, Extra Legroom Rows: {}, Exit Rows: {}",
                aircraft.getModel(), aircraft.getExtraLegroomRows(), aircraft.getExitRows());

        for (int row = 1; row <= aircraft.getTotalRows(); row++) {
            for (int i = 0; i < seatChars.size(); i++) {
                char seatChar = seatChars.get(i);
                String seatNumber = row + "" + seatChar;

                SeatType type = determineSeatType(i, seatChars.size(), aisleIndicesMap);

                boolean hasExtraLegroom = aircraft.getExtraLegroomRows() != null && aircraft.getExtraLegroomRows().contains(row);
                boolean isNearExit = aircraft.getExitRows() != null && aircraft.getExitRows().contains(row);
                boolean isOccupied = occupiedSeats.contains(seatNumber);

                Seat currentSeatDto = new Seat(seatNumber, row, seatChar, type, hasExtraLegroom, isNearExit, isOccupied);

                if (type == SeatType.WINDOW || hasExtraLegroom || isNearExit || seatNumber.equals("1A") || seatNumber.equals("8A")) {
                    log.debug("Seat {}: Type={}, ExtraL={}, NearExit={}, Occupied={}",
                            seatNumber, type, hasExtraLegroom, isNearExit, isOccupied);
                }

                seatMap.add(currentSeatDto);
            }
        }
        return seatMap;
    }


    /**
     * Soovitab istekohti vastavalt kasutaja eelistustele.
     *
     * @param flightId    Lennu ID.
     * @param preferences Kasutaja eelistused
     * @return List soovitatud istmetest.
     */
    public List<Seat> recommendSeats(Long flightId, SeatPreferences preferences) {
        log.info("Recommending seats for flight {} with preferences: {}", flightId, preferences);

        List<Seat> fullSeatMap = getSeatMap(flightId);
        Flight flight = flightRepository.findById(flightId).orElseThrow();
        Aircraft aircraft = flight.getAircraft();

        List<Seat> availableSeats = fullSeatMap.stream()
                .filter(seat -> !seat.isOccupied())
                .collect(Collectors.toList());

        log.debug("Found {} available seats.", availableSeats.size());

        List<Seat> potentialSeats = filterSeatsByPreferences(availableSeats, preferences);

        log.debug("Found {} potential seats after applying basic filters.", potentialSeats.size());


        if (potentialSeats.size() < preferences.getNumberOfSeats()) {
            log.warn("Not enough potential seats matching basic criteria found for flight {}.", flightId);
            return Collections.emptyList();
        }

        if (preferences.getNumberOfSeats() == 1) {
            return findBestSingleSeat(potentialSeats, preferences);
        } else {
            return findMultipleSeats(potentialSeats, preferences, aircraft);
        }
    }

    // --- Abimeetodid Soovitamiseks ---

    /** Filtreerib istmeid põhiliste eelistuste alusel */
    private List<Seat> filterSeatsByPreferences(List<Seat> seats, SeatPreferences prefs) {
        return seats.stream()
                .filter(seat -> prefs.getPreferredSeatType() == null || seat.getType() == prefs.getPreferredSeatType())
                .filter(seat -> !prefs.isRequireExtraLegroom() || seat.isHasExtraLegroom())
                .filter(seat -> !prefs.isPreferNearExit() || seat.isNearExit())
                .collect(Collectors.toList());
    }

    /** Leiab parima üksiku koha (lihtne prioriteet) */
    private List<Seat> findBestSingleSeat(List<Seat> potentialSeats, SeatPreferences prefs) {
        potentialSeats.sort(Comparator.comparing((Seat s) -> !s.isHasExtraLegroom())
                .thenComparing(s -> !s.isNearExit() && prefs.isPreferNearExit())
                .thenComparing(s -> s.getType() == SeatType.MIDDLE));

        if (!potentialSeats.isEmpty()) {
            log.info("Recommended single seat: {}", potentialSeats.get(0).getSeatNumber());
            return Collections.singletonList(potentialSeats.get(0));
        } else {
            log.warn("No single seat found matching criteria.");
            return Collections.emptyList();
        }
    }

    /** Leiab mitu kohta, eelistades kõrvuti asetsevaid */
    private List<Seat> findMultipleSeats(List<Seat> potentialSeats, SeatPreferences prefs, Aircraft aircraft) {
        int numSeatsNeeded = prefs.getNumberOfSeats();
        Map<Integer, List<Seat>> seatsByRow = potentialSeats.stream()
                .collect(Collectors.groupingBy(Seat::getRow));

        Map<Integer, Integer> aisleIndicesMap = getAisleIndices(aircraft.getSeatConfiguration());
        List<Character> seatCharsOrder = getSeatChars(aircraft);

        for (int row : seatsByRow.keySet().stream().sorted().toList()) {
            List<Seat> rowSeats = seatsByRow.get(row);
            if (rowSeats.size() < numSeatsNeeded) continue;

            rowSeats.sort(Comparator.comparingInt(s -> seatCharsOrder.indexOf(s.getSeatChar())));

            for (int i = 0; i <= rowSeats.size() - numSeatsNeeded; i++) {
                List<Seat> candidateGroup = rowSeats.subList(i, i + numSeatsNeeded);
                if (areSeatsAdjacent(candidateGroup, seatCharsOrder, aisleIndicesMap)) {
                    log.info("Found adjacent seats in row {}: {}", row, candidateGroup.stream().map(Seat::getSeatNumber).collect(Collectors.toList()));
                    return candidateGroup;
                }
            }
        }

        log.warn("Could not find strictly adjacent seats for {} people matching criteria.", numSeatsNeeded);

        if (prefs.isRequireAdjacentSeats()) {
            return Collections.emptyList();
        }

        potentialSeats.sort(Comparator.comparing((Seat s) -> !s.isHasExtraLegroom())
                .thenComparing(s -> !s.isNearExit() && prefs.isPreferNearExit())
                .thenComparing(s -> s.getType() == SeatType.MIDDLE));

        if (potentialSeats.size() >= numSeatsNeeded) {
            List<Seat> recommended = potentialSeats.subList(0, numSeatsNeeded);
            log.info("Recommending non-adjacent best available seats: {}", recommended.stream().map(Seat::getSeatNumber).collect(Collectors.toList()));
            return recommended;
        }

        log.error("Could not find enough seats ({}) even non-adjacent.", numSeatsNeeded);
        return Collections.emptyList();
    }

    /** Kontrollib, kas antud istmete grupp on füüsiliselt kõrvuti (arvestades vahekäike) */
    private boolean areSeatsAdjacent(List<Seat> seats, List<Character> seatCharsOrder, Map<Integer, Integer> aisleIndicesMap) {
        if (seats == null || seats.size() <= 1) {
            return true;
        }
        seats.sort(Comparator.comparingInt(s -> seatCharsOrder.indexOf(s.getSeatChar())));

        for (int i = 0; i < seats.size() - 1; i++) {
            Seat current = seats.get(i);
            Seat next = seats.get(i + 1);

            int currentIndex = seatCharsOrder.indexOf(current.getSeatChar());
            int nextIndex = seatCharsOrder.indexOf(next.getSeatChar());

            if (nextIndex != currentIndex + 1) {
                return false;
            }

            for(int aisleIndex : aisleIndicesMap.values()) {
                if(currentIndex == aisleIndex) {
                    return false;
                }
            }
        }
        return true;
    }

    // --- Abimeetodid Istekoha Plaani Jaoks (Võiks refaktoreerida) ---

    /** Määrab istme tüübi indeksi ja vahekäikude põhjal */
    private SeatType determineSeatType(int seatIndex, int totalSeatsInRow, Map<Integer, Integer> aisleIndicesMap) {
        if (seatIndex == 0 || seatIndex == totalSeatsInRow - 1) {
            return SeatType.WINDOW;
        }
        for (int aisleIndex : aisleIndicesMap.values()) {
            if (seatIndex == aisleIndex || seatIndex == aisleIndex + 1) {
                return SeatType.AISLE;
            }
        }
        return SeatType.MIDDLE;
    }

    /** Tagastab istmetähed listina vastavalt konfiguratsioonile */
    public List<Character> getSeatChars(Aircraft aircraft) {
        return aircraft.getSeatConfiguration().chars()
                .mapToObj(c -> (char) c)
                .filter(c -> c != '-')
                .collect(Collectors.toList());
    }

    /** Tagastab mapi, kus võti on vahekäigu järjekorranumber (0, 1, ...) ja väärtus on istme indeks, MILLE JÄREL vahekäik asub */
    public Map<Integer, Integer> getAisleIndices(String seatConfig) {
        Map<Integer, Integer> aislePositions = new HashMap<>();
        int seatIndex = -1;
        int aisleCount = 0;
        for (char c : seatConfig.toCharArray()) {
            if (c != '-') {
                seatIndex++;
            } else {
                aislePositions.put(aisleCount++, seatIndex);
            }
        }
        return aislePositions;
    }


    /**
     * Abimeetod hõivatud kohtade genereerimiseks või cache'ist lugemiseks.
     * Genereerib juhuslikult ~30-60% hõivatud kohti.
     */
    private Set<String> getOrGenerateOccupiedSeats(Long flightId, Aircraft aircraft) {
        return occupiedSeatsCache.computeIfAbsent(flightId, id -> {
            Set<String> generatedOccupied = new HashSet<>();
            int totalSeats = aircraft.getTotalRows() * getSeatChars(aircraft).size();
            int occupiedCount = (int) (totalSeats * (0.3 + random.nextDouble() * 0.3));

            List<String> allSeatNumbers = generateAllSeatNumbers(aircraft);
            Collections.shuffle(allSeatNumbers);

            generatedOccupied.addAll(allSeatNumbers.subList(0, Math.min(occupiedCount, allSeatNumbers.size())));
            log.debug("Generated {} occupied seats for flight {}", generatedOccupied.size(), flightId);
            return generatedOccupied;
        });
    }

    /**
     * Abimeetod kõigi võimalike istmenumbrite genereerimiseks lennuki jaoks.
     */
    private List<String> generateAllSeatNumbers(Aircraft aircraft) {
        List<String> allSeats = new ArrayList<>();
        List<Character> seatChars = getSeatChars(aircraft);

        for (int row = 1; row <= aircraft.getTotalRows(); row++) {
            for (char seatChar : seatChars) {
                allSeats.add(row + "" + seatChar);
            }
        }
        return allSeats;
    }
}