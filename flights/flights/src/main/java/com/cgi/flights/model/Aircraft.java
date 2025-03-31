package com.cgi.flights.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model; // nt. "Airbus A320", "ATR 72"

    private int totalRows; // Ridade arv lennukis

    // Istmete paigutus reas, nt "ABC-DEF" (vahekäik '-' kohal)
    private String seatConfiguration; // nt. "AC-DF" 2-2 ; "ABC-DEF" 3-3

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aircraft_extra_legroom_rows", joinColumns = @JoinColumn(name = "aircraft_id"))
    @Column(name = "row_number")
    private Set<Integer> extraLegroomRows;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "aircraft_exit_rows", joinColumns = @JoinColumn(name = "aircraft_id"))
    @Column(name = "row_number")
    private Set<Integer> exitRows;


}