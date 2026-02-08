package com.skypath.flightsearch.service;

import com.skypath.flightsearch.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final long MAX_LAYOVER_MIN = 6 * 60;   // 6h
    private static final long MIN_LAYOVER_DOM_MIN = 45;   // 45m
    private static final long MIN_LAYOVER_INTL_MIN = 90;  // 90m

    private final DataStore ds;

    public SearchService(DataStore ds) {
        this.ds = ds;
    }

    public List<Itinerary> search(String origin, String destination, LocalDate originLocalDate) {
        origin = origin.toUpperCase(Locale.ROOT);
        destination = destination.toUpperCase(Locale.ROOT);

        Airport originAp = ds.airports().get(origin);
        if (originAp == null) return List.of();

        ZoneId originZone = ZoneId.of(originAp.timezone());

        // Filter starting flights by origin and by *origin local date*.
        List<Flight> starters = ds.flightsByOrigin(origin).stream()
                .filter(f -> sameLocalDate(f.departureUtc(), originZone, originLocalDate))
                .toList();

        List<Itinerary> out = new ArrayList<>();
        for (Flight f : starters) {
            List<ItineraryLeg> legs = new ArrayList<>();
            legs.add(toLeg(f));
            dfs(origin, destination, legs, out);
        }

        out.sort(Comparator
                .comparing(Itinerary::totalPrice)
                .thenComparingLong(Itinerary::totalDurationMinutes)
                .thenComparingInt(i -> i.legs().size())
                .thenComparing(Itinerary::departureUtc)
        );
        return out;
    }

    private void dfs(String origin, String target, List<ItineraryLeg> legs, List<Itinerary> out) {
        ItineraryLeg last = legs.get(legs.size() - 1);
        if (last.destination().equals(target)) {
            out.add(buildItinerary(legs));
            // do not extend further once reached target
            return;
        }
        if (legs.size() >= 3) return; // supports direct (1), 1-stop (2), 2-stop (3)

        String connectFrom = last.destination();
        List<Flight> nexts = ds.flightsByOrigin(connectFrom);
        if (nexts.isEmpty()) return;

        // Avoid cycles: airports already used as origin or destination in path
        Set<String> used = new HashSet<>();
        used.add(origin);
        for (ItineraryLeg l : legs) used.add(l.destination());

        for (Flight nf : nexts) {
            // Airport-change rejection: require same airport code (already ensured by grouping)
            // but still check: last.dest == nf.origin
            if (!connectFrom.equals(nf.origin())) continue;

            // Prevent revisiting airports to avoid loops
            if (used.contains(nf.destination())) continue;

            long layMin = Duration.between(last.arrivalUtc(), nf.departureUtc()).toMinutes();
            if (layMin < 0) continue;

            long minRequired = isDomesticConnection(last, nf) ? MIN_LAYOVER_DOM_MIN : MIN_LAYOVER_INTL_MIN;
            if (layMin < minRequired) continue;
            if (layMin > MAX_LAYOVER_MIN) continue;

            legs.add(toLeg(nf));
            dfs(origin, target, legs, out);
            legs.remove(legs.size() - 1);
        }
    }

    private boolean isDomesticConnection(ItineraryLeg leg1, Flight leg2) {
        Airport a1o = ds.airports().get(leg1.origin());
        Airport a1d = ds.airports().get(leg1.destination());
        Airport a2o = ds.airports().get(leg2.origin());
        Airport a2d = ds.airports().get(leg2.destination());
        if (a1o == null || a1d == null || a2o == null || a2d == null) return false;

        String c1o = nz(a1o.country());
        String c1d = nz(a1d.country());
        String c2o = nz(a2o.country());
        String c2d = nz(a2d.country());

        // domestic layover if both legs are within the same country
        return c1o.equals(c1d) && c2o.equals(c2d) && c1d.equals(c2o);
    }

    private String nz(String s) { return s == null ? "" : s.trim().toUpperCase(Locale.ROOT); }

    private ItineraryLeg toLeg(Flight f) {
        return new ItineraryLeg(
                f.id(), f.origin(), f.destination(),
                f.departureUtc(), f.arrivalUtc(),
                f.price(), f.currency()
        );
    }

    private Itinerary buildItinerary(List<ItineraryLeg> legs) {
        BigDecimal sum = BigDecimal.ZERO;
        String currency = legs.get(0).currency();
        Instant dep = legs.get(0).departureUtc();
        Instant arr = legs.get(legs.size() - 1).arrivalUtc();

        for (ItineraryLeg l : legs) sum = sum.add(l.price());

        long totalMin = Duration.between(dep, arr).toMinutes();
        return new Itinerary(List.copyOf(legs), sum, currency, totalMin, dep, arr);
    }

    private boolean sameLocalDate(Instant utc, ZoneId zone, LocalDate date) {
        return utc.atZone(zone).toLocalDate().equals(date);
    }
}
