package com.agrihub.model;

/**
 * Response for GET /weather/estimate?location=...
 * <p>
 * IMPORTANT: recentRainfallMm comes from Open-Meteo's historical weather
 * archive, which is a weather-model / reanalysis-based estimate blended
 * with available station data - it is NOT exact rain-gauge measured
 * rainfall for the farmer's specific field. forecastRainfallMm is a
 * short-range forecast and carries normal forecast uncertainty.
 * estimatedWaterAvailability is a prototype classification using
 * configurable thresholds (see application.properties), not a validated
 * agronomic standard. The farmer should treat this as a starting point and
 * can always override the Water Availability field manually.
 */
public class WeatherEstimateResponse {

    private String queriedLocation;
    private String resolvedName;
    private Double latitude;
    private Double longitude;
    private Double recentRainfallMm;
    private Double forecastRainfallMm;
    private Double combinedRainfallMm;
    private String estimatedWaterAvailability;
    private final String source = "Open-Meteo (weather-model / reanalysis-based, not rain-gauge measured)";
    private final String disclaimer =
            "Recent rainfall is a weather-model / reanalysis-based estimate (Open-Meteo), not exact "
            + "rain-gauge measured rainfall for your specific field. Forecast rainfall is a short-range "
            + "forecast and can change. The Low/Medium/High classification uses configurable prototype "
            + "thresholds, not a validated agronomic standard. You can override this manually.";

    public WeatherEstimateResponse() {
    }

    public String getQueriedLocation() {
        return queriedLocation;
    }

    public void setQueriedLocation(String queriedLocation) {
        this.queriedLocation = queriedLocation;
    }

    public String getResolvedName() {
        return resolvedName;
    }

    public void setResolvedName(String resolvedName) {
        this.resolvedName = resolvedName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRecentRainfallMm() {
        return recentRainfallMm;
    }

    public void setRecentRainfallMm(Double recentRainfallMm) {
        this.recentRainfallMm = recentRainfallMm;
    }

    public Double getForecastRainfallMm() {
        return forecastRainfallMm;
    }

    public void setForecastRainfallMm(Double forecastRainfallMm) {
        this.forecastRainfallMm = forecastRainfallMm;
    }

    public Double getCombinedRainfallMm() {
        return combinedRainfallMm;
    }

    public void setCombinedRainfallMm(Double combinedRainfallMm) {
        this.combinedRainfallMm = combinedRainfallMm;
    }

    public String getEstimatedWaterAvailability() {
        return estimatedWaterAvailability;
    }

    public void setEstimatedWaterAvailability(String estimatedWaterAvailability) {
        this.estimatedWaterAvailability = estimatedWaterAvailability;
    }

    public String getSource() {
        return source;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}
