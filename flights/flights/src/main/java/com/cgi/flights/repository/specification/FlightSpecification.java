package com.cgi.flights.repository.specification;

import com.cgi.flights.model.Flight;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class FlightSpecification {

    public static Specification<Flight> getFlightsByCriteria(
            String destination,
            LocalDate date,
            BigDecimal maxPrice,
            Integer maxDurationMinutes
    ) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Sihtkoha filter
            if (StringUtils.hasText(destination)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("destination")), destination.toLowerCase()));
            }

            // 2. Kuupäeva filter
            if (date != null) {
                LocalDateTime startOfDay = date.atStartOfDay();
                LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.between(root.get("departureTime"), startOfDay, endOfDay));
            }

            // 3. Hinnafilter
            if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            // 4. Kestvuse filter (kasutades uut 'durationMinutes' välja)
            if (maxDurationMinutes != null && maxDurationMinutes > 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("durationMinutes"), maxDurationMinutes));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}