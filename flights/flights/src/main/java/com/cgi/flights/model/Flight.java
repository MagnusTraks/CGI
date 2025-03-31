package com.cgi.flights.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    private BigDecimal price;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Transient
    public Duration getDuration() {
        if (departureTime == null || arrivalTime == null) {
            return Duration.ZERO;
        }
        return Duration.between(departureTime, arrivalTime);
    }

    @Transient
    public long getDurationInMinutes() {
        return getDuration().toMinutes();
    }


    @PrePersist
    @PreUpdate
    public void calculateDurationMinutes() {
        if (departureTime != null && arrivalTime != null) {
            this.durationMinutes = (int) Duration.between(departureTime, arrivalTime).toMinutes();
        } else {
            this.durationMinutes = null;
        }
    }

    public Flight(Long id, String flightNumber, String origin, String destination, LocalDateTime departureTime, LocalDateTime arrivalTime, BigDecimal price, Aircraft aircraft) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
        this.aircraft = aircraft;
    }


}