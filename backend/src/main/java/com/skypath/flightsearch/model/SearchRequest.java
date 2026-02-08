package com.skypath.flightsearch.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class SearchRequest {
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "origin must be 3-letter airport code")
    private String origin;

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "destination must be 3-letter airport code")
    private String destination;

    // YYYY-MM-DD in origin local date
    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "date must be YYYY-MM-DD")
    private String date;

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
