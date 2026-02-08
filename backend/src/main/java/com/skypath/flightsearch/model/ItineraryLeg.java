package com.skypath.flightsearch.model;

import java.math.BigDecimal;
import java.time.Instant;

public record ItineraryLeg(
        String flightId,
        String origin,
        String destination,
        Instant departureUtc,
        Instant arrivalUtc,
        BigDecimal price,
        String currency
) {}
