package com.agrihub.service;

import com.agrihub.model.DailyWeather;
import com.agrihub.model.GeoLocation;
import com.agrihub.model.WeatherEstimateResponse;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single Open-Meteo integration shared by Water Estimation and the Dynamic
 * Crop Weather Calendar. No duplicate weather service is used.
 */
@Service
public class WeatherService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${agrihub.weather.rainfall.low-max-mm:30}")
    private double lowMaxMm;

    @Value("${agrihub.weather.rainfall.medium-max-mm:90}")
    private double mediumMaxMm;

    @Value("${agrihub.weather.rainfall.recent-days:7}")
    private int recentDays;

    @Value("${agrihub.weather.rainfall.forecast-days:5}")
    private int forecastDays;

    public WeatherEstimateResponse estimateWaterAvailability(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required");
        }

        JsonNode geo = geocodeWithFallbacks(location);
        if (geo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find that location. Try village/town with district/state, or select water availability manually.");
        }

        double latitude = geo.path("latitude").asDouble();
        double longitude = geo.path("longitude").asDouble();
        String resolvedName = buildResolvedName(geo);

        double recentRainfallMm = fetchRecentRainfallMm(latitude, longitude);
        double forecastRainfallMm = fetchForecastRainfallMm(latitude, longitude);
        double combinedRainfallMm = recentRainfallMm + forecastRainfallMm;

        WeatherEstimateResponse response = new WeatherEstimateResponse();
        response.setQueriedLocation(location);
        response.setResolvedName(resolvedName);
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setRecentRainfallMm(round1(recentRainfallMm));
        response.setForecastRainfallMm(round1(forecastRainfallMm));
        response.setCombinedRainfallMm(round1(combinedRainfallMm));
        response.setEstimatedWaterAvailability(classify(combinedRainfallMm));
        return response;
    }

    /** Resolve a location while trying progressively simpler variants. */
    public GeoLocation resolveLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required");
        }
        JsonNode geo = geocodeWithFallbacks(location);
        if (geo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Could not find that location. Try village/town with district/state.");
        }
        return new GeoLocation(
                geo.path("latitude").asDouble(),
                geo.path("longitude").asDouble(),
                buildResolvedName(geo));
    }

    /**
     * Forecast starting today. Values are returned only for dates Open-Meteo
     * actually provides. Daily humidity is calculated from real hourly RH data.
     */
    public List<DailyWeather> getDailyForecast(double latitude, double longitude, int days) {
        int safeDays = Math.max(1, Math.min(days, 16));
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s"
                        + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,et0_fao_evapotranspiration"
                        + "&hourly=relative_humidity_2m&forecast_days=%d&timezone=auto",
                latitude, longitude, safeDays);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return parseDailyWeather(response, "FORECAST");
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Recent past model data plus forecast data from Open-Meteo Forecast API.
     * This fills the latest few days that may not yet be available in the
     * long-term archive endpoint.
     */
    public List<DailyWeather> getRecentPastAndForecast(double latitude, double longitude,
                                                        int pastDays, int forecastDaysRequested) {
        int safePast = Math.max(0, Math.min(pastDays, 92));
        int safeForecast = Math.max(1, Math.min(forecastDaysRequested, 16));
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s"
                        + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,et0_fao_evapotranspiration"
                        + "&hourly=relative_humidity_2m&past_days=%d&forecast_days=%d&timezone=auto",
                latitude, longitude, safePast, safeForecast);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            List<DailyWeather> rows = parseDailyWeather(response, "FORECAST_OR_RECENT");
            LocalDate today = LocalDate.now();
            for (DailyWeather row : rows) {
                row.setDataType(row.getDate() != null && row.getDate().isBefore(today) ? "RECENT_PAST" : "FORECAST");
            }
            return rows;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Historical weather from Open-Meteo Archive. This method never invents
     * values for missing dates; callers must handle gaps explicitly.
     */
    public List<DailyWeather> getHistoricalDailyWeather(double latitude, double longitude,
                                                         LocalDate startDate, LocalDate endDate) {
        List<DailyWeather> empty = new ArrayList<>();
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) return empty;

        String url = String.format(
                "https://archive-api.open-meteo.com/v1/archive?latitude=%s&longitude=%s"
                        + "&start_date=%s&end_date=%s"
                        + "&daily=temperature_2m_max,temperature_2m_min,precipitation_sum,wind_speed_10m_max,et0_fao_evapotranspiration"
                        + "&timezone=auto",
                latitude, longitude, startDate, endDate);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return parseDailyWeather(response, "HISTORICAL");
        } catch (Exception e) {
            return empty;
        }
    }

    private List<DailyWeather> parseDailyWeather(JsonNode response, String dataType) {
        List<DailyWeather> result = new ArrayList<>();
        if (response == null) return result;

        Map<LocalDate, Double> humidityByDate = meanHourlyHumidity(response.path("hourly"));
        JsonNode daily = response.path("daily");
        JsonNode dates = daily.path("time");
        if (!dates.isArray()) return result;

        JsonNode maxT = daily.path("temperature_2m_max");
        JsonNode minT = daily.path("temperature_2m_min");
        JsonNode rain = daily.path("precipitation_sum");
        JsonNode wind = daily.path("wind_speed_10m_max");
        JsonNode et0 = daily.path("et0_fao_evapotranspiration");

        for (int i = 0; i < dates.size(); i++) {
            LocalDate date;
            try {
                date = LocalDate.parse(dates.get(i).asText());
            } catch (Exception ex) {
                continue;
            }
            DailyWeather row = new DailyWeather();
            row.setDate(date);
            row.setTemperatureMaxC(valueAt(maxT, i));
            row.setTemperatureMinC(valueAt(minT, i));
            row.setPrecipitationMm(valueAt(rain, i));
            row.setWindSpeedMaxKmh(valueAt(wind, i));
            row.setEt0Mm(valueAt(et0, i));
            row.setHumidityMeanPct(humidityByDate.get(date));
            row.setDataType(dataType);
            result.add(row);
        }
        return result;
    }

    private Map<LocalDate, Double> meanHourlyHumidity(JsonNode hourly) {
        Map<LocalDate, double[]> sums = new LinkedHashMap<>();
        if (hourly == null || hourly.isMissingNode()) return new LinkedHashMap<>();
        JsonNode times = hourly.path("time");
        JsonNode rh = hourly.path("relative_humidity_2m");
        if (!times.isArray() || !rh.isArray()) return new LinkedHashMap<>();

        int n = Math.min(times.size(), rh.size());
        for (int i = 0; i < n; i++) {
            if (rh.get(i) == null || rh.get(i).isNull()) continue;
            try {
                LocalDate date = LocalDateTime.parse(times.get(i).asText(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
                double[] agg = sums.computeIfAbsent(date, d -> new double[]{0.0, 0.0});
                agg[0] += rh.get(i).asDouble();
                agg[1] += 1.0;
            } catch (Exception ignored) {
            }
        }

        Map<LocalDate, Double> means = new LinkedHashMap<>();
        for (Map.Entry<LocalDate, double[]> e : sums.entrySet()) {
            if (e.getValue()[1] > 0) means.put(e.getKey(), round1(e.getValue()[0] / e.getValue()[1]));
        }
        return means;
    }

    private Double valueAt(JsonNode array, int index) {
        if (array == null || !array.isArray() || index >= array.size() || array.get(index) == null || array.get(index).isNull()) {
            return null;
        }
        return array.get(index).asDouble();
    }

    private JsonNode geocodeWithFallbacks(String location) {
        List<String> candidates = new ArrayList<>();
        String cleaned = location == null ? "" : location.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) return null;
        candidates.add(cleaned);

        String[] parts = cleaned.split(",");
        List<String> tokens = new ArrayList<>();
        for (String part : parts) {
            String t = part.trim();
            if (!t.isEmpty()) tokens.add(t);
        }
        if (tokens.size() >= 3) {
            candidates.add(tokens.get(0) + ", " + tokens.get(tokens.size() - 1));
            candidates.add(tokens.get(0));
            candidates.add(tokens.get(1) + ", " + tokens.get(tokens.size() - 1));
        } else if (tokens.size() == 2) {
            candidates.add(tokens.get(0));
        }

        for (String candidate : candidates) {
            JsonNode match = geocode(candidate);
            if (match != null) return match;
        }
        return null;
    }

    private JsonNode geocode(String location) {
        String encoded = URLEncoder.encode(location, StandardCharsets.UTF_8);
        String url = "https://geocoding-api.open-meteo.com/v1/search?name=" + encoded
                + "&count=5&language=en&format=json&countryCode=IN";
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            if (response == null) return null;
            JsonNode results = response.path("results");
            if (!results.isArray() || results.isEmpty()) return null;
            return results.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildResolvedName(JsonNode geo) {
        List<String> parts = new ArrayList<>();
        addIfPresent(parts, geo, "name");
        addIfPresent(parts, geo, "admin2");
        addIfPresent(parts, geo, "admin1");
        addIfPresent(parts, geo, "country");
        return String.join(", ", parts.stream().distinct().toList());
    }

    private void addIfPresent(List<String> parts, JsonNode node, String field) {
        if (node.hasNonNull(field)) {
            String value = node.get(field).asText().trim();
            if (!value.isEmpty()) parts.add(value);
        }
    }

    private double fetchRecentRainfallMm(double latitude, double longitude) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(Math.max(recentDays - 1, 0));
        String url = String.format(
                "https://archive-api.open-meteo.com/v1/archive?latitude=%s&longitude=%s"
                        + "&start_date=%s&end_date=%s&daily=precipitation_sum&timezone=auto",
                latitude, longitude, startDate, endDate);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            JsonNode dates = response == null ? null : response.path("daily").path("time");
            if (dates != null && dates.isArray() && !dates.isEmpty()) {
                return sumDailyArray(response);
            }
        } catch (Exception ignored) {
            // Fall through to recent model data below.
        }

        // Archive data can lag by a few days. Use Open-Meteo's recent past
        // forecast-model data as a fallback rather than silently returning 0.
        double total = 0.0;
        LocalDate today = LocalDate.now();
        for (DailyWeather row : getRecentPastAndForecast(latitude, longitude, Math.min(recentDays, 92), 1)) {
            if (row.getDate() != null && row.getDate().isBefore(today) && row.getDate().isAfter(startDate.minusDays(1))) {
                total += row.getPrecipitationMm() != null ? row.getPrecipitationMm() : 0.0;
            }
        }
        return total;
    }

    private double fetchForecastRainfallMm(double latitude, double longitude) {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s"
                        + "&daily=precipitation_sum&forecast_days=%d&timezone=auto",
                latitude, longitude, forecastDays);
        try {
            JsonNode response = restTemplate.getForObject(url, JsonNode.class);
            return sumDailyArray(response);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double sumDailyArray(JsonNode response) {
        if (response == null) return 0.0;
        JsonNode values = response.path("daily").path("precipitation_sum");
        if (!values.isArray()) return 0.0;
        double total = 0.0;
        for (JsonNode v : values) {
            if (v != null && !v.isNull()) total += v.asDouble();
        }
        return total;
    }

    private String classify(double combinedRainfallMm) {
        if (combinedRainfallMm <= lowMaxMm) return "Low";
        if (combinedRainfallMm <= mediumMaxMm) return "Medium";
        return "High";
    }

    private Double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
