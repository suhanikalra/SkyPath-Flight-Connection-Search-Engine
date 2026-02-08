package com.skypath.flightsearch.model;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ItineraryResponse {

    private BigDecimal totalPrice;
    private String currency;
    private long totalDurationMinutes;
    private int stops;
    private Instant departureUtc;
    private Instant arrivalUtc;
    private List<LegResponse> legs;

    public ItineraryResponse(BigDecimal totalPrice,
                             String currency,
                             long totalDurationMinutes,
                             int stops,
                             Instant departureUtc,
                             Instant arrivalUtc,
                             List<LegResponse> legs) {
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.totalDurationMinutes = totalDurationMinutes;
        this.stops = stops;
        this.departureUtc = departureUtc;
        this.arrivalUtc = arrivalUtc;
        this.legs = legs;
    }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getCurrency() { return currency; }
    public long getTotalDurationMinutes() { return totalDurationMinutes; }
    public int getStops() { return stops; }
    public Instant getDepartureUtc() { return departureUtc; }
    public Instant getArrivalUtc() { return arrivalUtc; }
    public List<LegResponse> getLegs() { return legs; }
}
