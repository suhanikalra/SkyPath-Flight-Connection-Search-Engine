package com.skypath.flightsearch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypath.flightsearch.model.Airport;
import com.skypath.flightsearch.model.Flight;
import com.skypath.flightsearch.model.RawFlight;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Loads airports + flights from resources/data.json.
 * - Ignores invalid / dirty records silently.
 * - Converts local times to UTC Instants using airport timezones.
 */
@Component
public class DataStore {

    private final ObjectMapper om = new ObjectMapper();

    private final Map<String, Airport> airports = new ConcurrentHashMap<>();
    private final List<Flight> flights = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<Flight>> flightsByOrigin = new ConcurrentHashMap<>();

    public Map<String, Airport> airports() { return airports; }

    public List<Flight> flightsByOrigin(String origin) {
        return flightsByOrigin.getOrDefault(origin, List.of());
    }

    @PostConstruct
    public void load() {
        try (InputStream is = new ClassPathResource("data.json").getInputStream()) {
            JsonNode root = om.readTree(is);

            // airports
            JsonNode ap = root.get("airports");
            if (ap != null && ap.isArray()) {
                for (JsonNode n : ap) {
                    try {
                        String code = text(n, "code");
                        if (code == null || !code.matches("^[A-Z]{3}$")) continue;
                        String tz = text(n, "timezone");
                        if (tz == null) continue;
                        ZoneId.of(tz); // validate

                        Airport a = new Airport(
                                code,
                                text(n, "name"),
                                text(n, "city"),
                                text(n, "country"),
                                tz
                        );
                        airports.put(code, a);
                    } catch (Exception ignored) {}
                }
            }

            // flights
            JsonNode fl = root.get("flights");
            if (fl != null && fl.isArray()) {
                for (JsonNode n : fl) {
                    try {
                        RawFlight rf = om.treeToValue(n, RawFlight.class);
                        Flight f = normalizeFlight(rf);
                        if (f != null) flights.add(f);
                    } catch (Exception ignored) {}
                }
            }

            // index
            Map<String, List<Flight>> map = flights.stream()
                    .collect(Collectors.groupingBy(Flight::origin));

            for (var e : map.entrySet()) {
                List<Flight> list = new ArrayList<>(e.getValue());
                list.sort(Comparator.comparing(Flight::departureUtc));
                flightsByOrigin.put(e.getKey(), Collections.unmodifiableList(list));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load data.json: " + e.getMessage(), e);
        }
    }

    private Flight normalizeFlight(RawFlight rf) {
        if (rf == null) return null;
        String id = safe(rf.id);
        String o = safe(rf.origin);
        String d = safe(rf.destination);
        if (o == null || d == null) return null;
        o = o.toUpperCase(Locale.ROOT);
        d = d.toUpperCase(Locale.ROOT);
        if (!o.matches("^[A-Z]{3}$") || !d.matches("^[A-Z]{3}$")) return null;
        if (!airports.containsKey(o) || !airports.containsKey(d)) return null;

        String depLocal = safe(rf.departureLocal);
        String arrLocal = safe(rf.arrivalLocal);
        if (depLocal == null || arrLocal == null) return null;

        Instant depUtc;
        Instant arrUtc;
        try {
            ZoneId oz = ZoneId.of(airports.get(o).timezone());
            ZoneId dz = ZoneId.of(airports.get(d).timezone());
            LocalDateTime depLdt = LocalDateTime.parse(depLocal);
            LocalDateTime arrLdt = LocalDateTime.parse(arrLocal);

            depUtc = depLdt.atZone(oz).toInstant();
            arrUtc = arrLdt.atZone(dz).toInstant();
        } catch (DateTimeException ex) {
            return null;
        }

        if (!arrUtc.isAfter(depUtc)) return null; // invalid time

        BigDecimal price = parsePrice(rf.price);
        if (price == null || price.signum() < 0) return null;
        String cur = safe(rf.currency);
        if (cur == null) cur = "USD";

        return new Flight(
                id != null ? id : (o + "-" + d + "-" + depUtc.toEpochMilli()),
                o, d,
                depUtc, arrUtc,
                price, cur
        );
    }

    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static String text(JsonNode n, String k) {
        JsonNode v = n.get(k);
        if (v == null || v.isNull()) return null;
        String t = v.asText(null);
        if (t == null) return null;
        t = t.trim();
        return t.isEmpty() ? null : t;
    }

    private static BigDecimal parsePrice(Object p) {
        if (p == null) return null;
        try {
            if (p instanceof Number num) return new BigDecimal(num.toString());
            if (p instanceof String s) {
                String t = s.trim();
                if (t.isEmpty()) return null;
                // allow "123.45", "$123.45", "USD 123"
                t = t.replaceAll("[^0-9.\\-]", "");
                if (t.isEmpty() || t.equals(".") || t.equals("-") || t.equals("-."))
                    return null;
                return new BigDecimal(t);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
