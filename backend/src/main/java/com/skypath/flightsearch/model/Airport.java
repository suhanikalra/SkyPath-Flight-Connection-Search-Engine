package com.skypath.flightsearch.model;

public record Airport(
        String code,
        String name,
        String city,
        String country,
        String timezone
) {}
