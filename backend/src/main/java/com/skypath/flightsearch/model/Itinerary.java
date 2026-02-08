package com.skypath.flightsearch.model;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record Itinerary(
        List<ItineraryLeg> legs,
        BigDecimal totalPrice,
        String currency,
        long totalDurationMinutes,
        Instant departureUtc,
        Instant arrivalUtc
) {}
