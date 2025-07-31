package com.azarenka.evebuilders.domain.dto;

public class LocationInfo {
    private Long locationId;
    private String name;
    private String type; // SYSTEM / STATION / STRUCTURE

    public LocationInfo(Long locationId, String name, String type) {
        this.locationId = locationId;
        this.name = name;
        this.type = type;
    }

    public Long getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}

