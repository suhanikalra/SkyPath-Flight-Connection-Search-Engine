package com.skypath.flightsearch.api;

import com.skypath.flightsearch.model.*;
import com.skypath.flightsearch.service.DataStore;
import com.skypath.flightsearch.service.SearchService;
import com.skypath.flightsearch.service.TimeFormatUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api")
public class SearchApi {

    private final SearchService searchService;
    private final DataStore dataStore;

    public SearchApi(SearchService searchService, DataStore dataStore) {
        this.searchService = searchService;
        this.dataStore = dataStore;
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @Valid @RequestBody SearchRequest searchRequest
    ) {
        LocalDate date = LocalDate.parse(searchRequest.getDate());

        List<Itinerary> itineraries =
                searchService.search(searchRequest.getOrigin(), searchRequest.getDestination(), date);

        List<ItineraryResponse> responseItineraries = new ArrayList<>();

        for (Itinerary itinerary : itineraries) {

            List<LegResponse> legResponse = new ArrayList<>();
            for (ItineraryLeg itineraryLeg : itinerary.legs()) {
                Airport origin = dataStore.airports().get(itineraryLeg.origin());
                Airport destination = dataStore.airports().get(itineraryLeg.destination());

                legResponse.add(new LegResponse(
                        itineraryLeg.flightId(),
                        itineraryLeg.origin(),
                        itineraryLeg.destination(),
                        itineraryLeg.price(),
                        itineraryLeg.currency(),
                        itineraryLeg.departureUtc(),
                        itineraryLeg.arrivalUtc(),
                        TimeFormatUtil.formatLocal(itineraryLeg.departureUtc(), origin),
                        TimeFormatUtil.formatLocal(itineraryLeg.arrivalUtc(), destination)
                ));
            }

            responseItineraries.add(new ItineraryResponse(
                    itinerary.totalPrice(),
                    itinerary.currency(),
                    itinerary.totalDurationMinutes(),
                    Math.max(0, itinerary.legs().size() - 1),
                    itinerary.departureUtc(),
                    itinerary.arrivalUtc(),
                    legResponse
            ));
        }

        SearchResponse response = new SearchResponse(
                new SearchQuery(
                        searchRequest.getOrigin().toUpperCase(),
                        searchRequest.getDestination().toUpperCase(),
                        searchRequest.getDate()
                ),
                responseItineraries.size(),
                responseItineraries
        );

        return ResponseEntity.ok(response);
    }





}
