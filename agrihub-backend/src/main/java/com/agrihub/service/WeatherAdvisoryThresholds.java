package com.agrihub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centralized, configurable thresholds used by the Crop Calendar's
 * weather-adaptive advisories (see application.properties).
 * <p>
 * These are PROTOTYPE decision thresholds used for demonstration purposes
 * in this project. They are NOT validated universal agronomic or
 * meteorological standards, and should be tuned per crop, region, and
 * local agricultural extension guidance before being relied on for real
 * farming decisions.
 */
@Component
public class WeatherAdvisoryThresholds {

    @Value("${agrihub.cropcalendar.rain-warning-mm:10}")
    private double rainWarningMm;

    @Value("${agrihub.cropcalendar.heavy-rain-mm:30}")
    private double heavyRainMm;

    @Value("${agrihub.cropcalendar.high-temperature-c:38}")
    private double highTemperatureC;

    @Value("${agrihub.cropcalendar.strong-wind-kmh:25}")
    private double strongWindKmh;

    @Value("${agrihub.cropcalendar.forecast-window-days:16}")
    private int forecastWindowDays;

    @Value("${agrihub.cropcalendar.reschedule-lookahead-days:5}")
    private int rescheduleLookaheadDays;

    public double getRainWarningMm() {
        return rainWarningMm;
    }

    public double getHeavyRainMm() {
        return heavyRainMm;
    }

    public double getHighTemperatureC() {
        return highTemperatureC;
    }

    public double getStrongWindKmh() {
        return strongWindKmh;
    }

    public int getForecastWindowDays() {
        return forecastWindowDays;
    }

    public int getRescheduleLookaheadDays() {
        return rescheduleLookaheadDays;
    }
}
