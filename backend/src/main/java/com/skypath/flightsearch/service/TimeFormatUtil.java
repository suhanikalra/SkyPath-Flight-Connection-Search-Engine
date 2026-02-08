package com.skypath.flightsearch.service;

import com.skypath.flightsearch.model.Airport;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class TimeFormatUtil {
    private TimeFormatUtil() {}

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm z");

    public static String formatLocal(Instant utc, Airport airport) {
        if (utc == null || airport == null || airport.timezone() == null) return null;
        return utc.atZone(ZoneId.of(airport.timezone())).format(FMT);
    }
}
