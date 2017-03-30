package com.example.shopService.support.geolocation;


import lombok.Data;

@Data
public class Coordinates {
    private final double longitude;
    private final double latitude;

    Coordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
