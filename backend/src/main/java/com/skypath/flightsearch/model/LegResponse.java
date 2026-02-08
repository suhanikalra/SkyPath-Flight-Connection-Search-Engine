package com.skypath.flightsearch.model;


import java.math.BigDecimal;
import java.time.Instant;

public class LegResponse {

    private String flightId;
    private String origin;
    private String destination;
    private BigDecimal price;
    private String currency;
    private Instant departureUtc;
    private Instant arrivalUtc;
    private String departureLocal;
    private String arrivalLocal;

    public LegResponse(String flightId,
                       String origin,
                       String destination,
                       BigDecimal price,
                       String currency,
                       Instant departureUtc,
                       Instant arrivalUtc,
                       String departureLocal,
                       String arrivalLocal) {
        this.flightId = flightId;
        this.origin = origin;
        this.destination = destination;
        this.price = price;
        this.currency = currency;
        this.departureUtc = departureUtc;
        this.arrivalUtc = arrivalUtc;
        this.departureLocal = departureLocal;
        this.arrivalLocal = arrivalLocal;
    }

    public String getFlightId() { return flightId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public BigDecimal getPrice() { return price; }
    public String getCurrency() { return currency; }
    public Instant getDepartureUtc() { return departureUtc; }
    public Instant getArrivalUtc() { return arrivalUtc; }
    public String getDepartureLocal() { return departureLocal; }
    public String getArrivalLocal() { return arrivalLocal; }
}
