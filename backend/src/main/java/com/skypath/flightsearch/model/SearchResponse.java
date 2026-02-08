package com.skypath.flightsearch.model;


import java.util.List;

public class SearchResponse {

    private SearchQuery query;
    private int count;
    private List<ItineraryResponse> itineraries;

    public SearchResponse(SearchQuery query,
                          int count,
                          List<ItineraryResponse> itineraries) {
        this.query = query;
        this.count = count;
        this.itineraries = itineraries;
    }

    public SearchQuery getQuery() {
        return query;
    }

    public int getCount() {
        return count;
    }

    public List<ItineraryResponse> getItineraries() {
        return itineraries;
    }
}

