package com.skypath.flightsearch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawFlight {
    public String id;
    public String origin;
    public String destination;
    public String departureLocal; // e.g., "2026-02-10T09:30"
    public String arrivalLocal;   // e.g., "2026-02-10T11:10"
    public Object price;          // can be number/string/null
    public String currency;       // e.g., "USD"
}
