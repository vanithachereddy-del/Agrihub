package com.agrihub.model;

/**
 * Resolved location coordinates, shared by WeatherService's existing water
 * availability estimate and the newer Crop Calendar feature - avoids a
 * second, duplicate geocoding implementation.
 */
public class GeoLocation {

    private double latitude;
    private double longitude;
    private String resolvedName;

    public GeoLocation() {
    }

    public GeoLocation(double latitude, double longitude, String resolvedName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.resolvedName = resolvedName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getResolvedName() {
        return resolvedName;
    }

    public void setResolvedName(String resolvedName) {
        this.resolvedName = resolvedName;
    }
}
