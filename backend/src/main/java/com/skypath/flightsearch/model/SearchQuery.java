package com.skypath.flightsearch.model;


public class SearchQuery {

    private String origin;
    private String destination;
    private String date;

    public SearchQuery(String origin, String destination, String date) {
        this.origin = origin;
        this.destination = destination;
        this.date = date;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getDate() {
        return date;
    }
}

