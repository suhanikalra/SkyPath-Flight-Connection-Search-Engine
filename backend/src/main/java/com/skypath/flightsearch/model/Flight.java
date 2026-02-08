package com.skypath.flightsearch.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Flight(
        String id,
        String origin,
        String destination,
        Instant departureUtc,
        Instant arrivalUtc,
        BigDecimal price,
        String currency
) {}
