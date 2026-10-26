package com.agrihub.service;

import com.agrihub.model.CropCalendarActivity;
import com.agrihub.model.CropCalendarRequest;
import com.agrihub.model.CropCalendarResponse;
import com.agrihub.model.CropWeatherSummary;
import com.agrihub.model.DailyWeather;
import com.agrihub.model.GeoLocation;
import com.agrihub.model.StageWaterStatus;
import com.agrihub.model.WaterStatus;
import com.agrihub.model.WeatherAlert;
import com.agrihub.model.WeatherThresholdInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Enhanced Dynamic Weather-Adaptive Crop Calendar.
 *
 * The service preserves the existing rule-based activity calendar, adds real
 * Open-Meteo weather data, historical/forecast GDD, estimated crop stage,
 * stage-aware alerts, ET0-based prototype water status, and richer activity
 * advisories. No weather values are fabricated outside data returned by the API.
 */
@Service
public class CropCalendarService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);

    @Autowired
    private CropCalendarTemplateProvider templateProvider;

    @Autowired
    private CropWeatherProfileRegistry profileRegistry;

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherAdvisoryThresholds advisoryThresholds;

    public CropCalendarResponse generate(CropCalendarRequest request) {
        validateRequest(request);

        CropCalendarTemplateProvider.CropTemplate template = templateProvider.getTemplate(request.getCrop());
        if (template == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Calendar not available yet for '" + request.getCrop() + "'. Supported crops: "
                            + String.join(", ", templateProvider.getSupportedCropNames()));
        }

        CropWeatherProfileRegistry.CropWeatherProfile profile = profileRegistry.getProfile(template.cropName);
        if (profile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Weather profile is not configured for " + template.cropName);
        }

        LocalDate sowingDate = parseDate(request.getSowingDate());
        LocalDate today = LocalDate.now();
        String durationProfile = normalizeDurationProfile(request.getCropVarietyDuration());
        int[] durationRange = selectDurationRange(template.durationMinDays, template.durationMaxDays, durationProfile);
        LocalDate harvestStart = sowingDate.plusDays(durationRange[0]);
        LocalDate harvestEnd = sowingDate.plusDays(durationRange[1]);
        long daysAfterSowing = sowingDate.isAfter(today) ? 0 : ChronoUnit.DAYS.between(sowingDate, today);

        String locationQuery = buildLocationQuery(request);
        GeoLocation geo = null;
        List<DailyWeather> weatherTimeline = new ArrayList<>();
        List<DailyWeather> forecastRows = new ArrayList<>();
        boolean weatherAvailable = false;

        if (locationQuery != null) {
            try {
                geo = weatherService.resolveLocation(locationQuery);
                weatherTimeline = loadWeatherTimeline(geo, sowingDate, today);
                calculateGddAndStages(weatherTimeline, profile, sowingDate);
                forecastRows = weatherTimeline.stream()
                        .filter(w -> w.getDate() != null && !w.getDate().isBefore(today))
                        .filter(w -> "FORECAST".equals(w.getDataType()))
                        .sorted(Comparator.comparing(DailyWeather::getDate))
                        .limit(7)
                        .toList();
                weatherAvailable = !weatherTimeline.isEmpty();
            } catch (ResponseStatusException ex) {
                geo = null;
            }
        }

        Double accumulatedGdd = null;
        String currentStage;
        if (sowingDate.isAfter(today)) {
            accumulatedGdd = 0.0;
            currentStage = "Pre-Sowing";
        } else if (weatherAvailable) {
            accumulatedGdd = currentAccumulatedGdd(weatherTimeline, today);
            currentStage = profile.stageForGdd(accumulatedGdd != null ? accumulatedGdd : 0.0).getName();
        } else {
            currentStage = "Weather data unavailable";
        }

        if (!forecastRows.isEmpty()) {
            for (DailyWeather row : forecastRows) {
                row.setAdvisory(buildDailyAdvisory(row, profile));
            }
        }

        List<CropCalendarActivity> activities = new ArrayList<>();
        LocalDate forecastHorizon = forecastRows.isEmpty() ? null : forecastRows.get(forecastRows.size() - 1).getDate();
        Map<LocalDate, DailyWeather> timelineByDate = indexByDate(weatherTimeline);
        for (CropCalendarTemplateProvider.ActivityTemplate activityTemplate : template.activities) {
            activities.add(buildActivity(activityTemplate, sowingDate, today, timelineByDate,
                    weatherAvailable, forecastHorizon, request.getIrrigationSource(), profile));
        }
        activities.add(buildHarvestActivity(durationRange, harvestStart, harvestEnd));

        CropCalendarResponse response = new CropCalendarResponse();
        response.setCrop(template.cropName);
        response.setCropVarietyDuration(durationProfile);
        response.setSeason(blankToDefault(request.getSeason(), "Not specified"));
        response.setSowingDate(sowingDate.format(DISPLAY));
        response.setDurationLabel(formatDuration(durationRange, durationProfile));
        response.setHarvestStart(harvestStart.format(DISPLAY));
        response.setHarvestEnd(harvestEnd.format(DISPLAY));
        response.setQueriedLocation(locationQuery);
        response.setResolvedLocation(geo != null ? geo.getResolvedName() : null);
        response.setWeatherAvailable(weatherAvailable);
        response.setForecastAvailableUntil(forecastHorizon != null ? forecastHorizon.format(DISPLAY) : null);
        response.setDaysAfterSowing(daysAfterSowing);
        response.setCurrentStage(currentStage);
        response.setBaseTemperature(round1(profile.getBaseTemperatureC()));
        response.setBaseTemperatureSourceNote(profile.getBaseTemperatureSourceNote());
        response.setAccumulatedGdd(accumulatedGdd != null ? round1(accumulatedGdd) : null);
        response.setCurrentWeatherSummary(buildCurrentSummary(forecastRows));
        response.setDailyForecast(new ArrayList<>(forecastRows));
        response.setCurrentStageThresholds(buildThresholdInfo(profile, currentStage));
        response.setWeatherAlerts(buildWeatherAlerts(forecastRows, profile, currentStage));
        response.setWaterStatus(buildWaterStatus(weatherTimeline, sowingDate, today));
        response.setStageWaterTable(buildStageWaterTable(weatherTimeline, sowingDate, today));
        response.setActivities(activities);
        response.setDisclaimer(
                "Estimated Crop Stage is based on accumulated GDD calculated from Open-Meteo temperature data and a crop-specific base temperature. "
                        + "Stage GDD boundaries and stage weather thresholds are configurable prototype agronomic values and are not official universal standards. "
                        + "The water section uses Open-Meteo reference evapotranspiration (ET0) only as a prototype estimated water requirement; it is not exact crop evapotranspiration. "
                        + "Forecast weather can change and does not guarantee actual rainfall. Activities outside the available forecast range are never assigned fabricated weather values. "
                        + "Validate field decisions with local agricultural guidance and actual field conditions.");
        return response;
    }

    private void validateRequest(CropCalendarRequest request) {
        if (request == null || request.getCrop() == null || request.getCrop().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Crop is required");
        }
        if (request.getSowingDate() == null || request.getSowingDate().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sowing date is required");
        }
    }

    private List<DailyWeather> loadWeatherTimeline(GeoLocation geo, LocalDate sowingDate, LocalDate today) {
        TreeMap<LocalDate, DailyWeather> merged = new TreeMap<>();

        // Archive is used for older past dates. The latest five days are filled
        // from Open-Meteo's forecast endpoint using past_days to avoid archive lag.
        LocalDate archiveEnd = today.minusDays(6);
        if (!sowingDate.isAfter(archiveEnd)) {
            List<DailyWeather> historical = weatherService.getHistoricalDailyWeather(
                    geo.getLatitude(), geo.getLongitude(), sowingDate, archiveEnd);
            for (DailyWeather row : historical) {
                if (row.getDate() != null) merged.put(row.getDate(), row);
            }
        }

        List<DailyWeather> recentAndForecast = weatherService.getRecentPastAndForecast(
                geo.getLatitude(), geo.getLongitude(), 5, advisoryThresholds.getForecastWindowDays());
        for (DailyWeather row : recentAndForecast) {
            if (row.getDate() != null) merged.put(row.getDate(), row);
        }

        return new ArrayList<>(merged.values());
    }

    private void calculateGddAndStages(List<DailyWeather> rows,
                                       CropWeatherProfileRegistry.CropWeatherProfile profile,
                                       LocalDate sowingDate) {
        rows.sort(Comparator.comparing(DailyWeather::getDate));
        double cumulative = 0.0;
        for (DailyWeather row : rows) {
            if (row.getDate() == null || row.getDate().isBefore(sowingDate)) continue;
            Double max = row.getTemperatureMaxC();
            Double min = row.getTemperatureMinC();
            if (max == null || min == null) {
                row.setDailyGdd(null);
                row.setCumulativeGdd(round1(cumulative));
                row.setCropStage(profile.stageForGdd(cumulative).getName());
                continue;
            }
            double daily = Math.max(0.0, ((max + min) / 2.0) - profile.getBaseTemperatureC());
            cumulative += daily;
            row.setDailyGdd(round1(daily));
            row.setCumulativeGdd(round1(cumulative));
            row.setCropStage(profile.stageForGdd(cumulative).getName());
        }
    }

    private Double currentAccumulatedGdd(List<DailyWeather> rows, LocalDate today) {
        Double result = null;
        for (DailyWeather row : rows) {
            if (row.getDate() != null && !row.getDate().isAfter(today) && row.getCumulativeGdd() != null) {
                result = row.getCumulativeGdd();
            }
        }
        return result;
    }

    private CropWeatherSummary buildCurrentSummary(List<DailyWeather> forecastRows) {
        if (forecastRows == null || forecastRows.isEmpty()) return null;
        DailyWeather row = forecastRows.get(0);
        CropWeatherSummary summary = new CropWeatherSummary();
        summary.setMaxTemp(roundNullable(row.getTemperatureMaxC()));
        summary.setMinTemp(roundNullable(row.getTemperatureMinC()));
        summary.setRainfall(roundNullable(row.getPrecipitationMm()));
        summary.setHumidity(roundNullable(row.getHumidityMeanPct()));
        summary.setWindSpeed(roundNullable(row.getWindSpeedMaxKmh()));
        return summary;
    }

    private WeatherThresholdInfo buildThresholdInfo(CropWeatherProfileRegistry.CropWeatherProfile profile,
                                                     String currentStage) {
        WeatherThresholdInfo info = new WeatherThresholdInfo();
        info.setStage(currentStage);
        if (currentStage == null || currentStage.startsWith("Weather") || "Pre-Sowing".equals(currentStage)) {
            info.setValidationNote("Stage-specific thresholds become available after a weather-based crop stage can be estimated.");
            return info;
        }
        CropWeatherProfileRegistry.StageConfig stage = profile.stageByName(currentStage);
        info.setMaxTemperatureC(stage.getMaxTemperatureC());
        info.setMinTemperatureC(stage.getMinTemperatureC());
        info.setRainfallThresholdMm(stage.getRainfallThresholdMm());
        info.setMinHumidityPct(stage.getMinHumidityPct());
        info.setMaxHumidityPct(stage.getMaxHumidityPct());
        info.setValidationNote("Configurable prototype crop/stage thresholds — validate for local variety and region before field use.");
        return info;
    }

    private List<WeatherAlert> buildWeatherAlerts(List<DailyWeather> forecastRows,
                                                   CropWeatherProfileRegistry.CropWeatherProfile profile,
                                                   String currentStage) {
        List<WeatherAlert> alerts = new ArrayList<>();
        if (forecastRows == null || forecastRows.isEmpty()) {
            alerts.add(new WeatherAlert("INFO", "info", "Forecast Not Available",
                    "No forecast values are available for the selected location right now.", null));
            return alerts;
        }

        boolean highTempAdded = false;
        boolean lowTempAdded = false;
        boolean rainAdded = false;
        boolean humidityAdded = false;
        boolean windAdded = false;
        boolean irrigationAdded = false;

        for (DailyWeather day : forecastRows) {
            CropWeatherProfileRegistry.StageConfig stage = day.getCropStage() != null
                    ? profile.stageByName(day.getCropStage())
                    : profile.stageByName(currentStage);
            String date = day.getDate() != null ? day.getDate().format(DISPLAY) : null;

            if (!highTempAdded && day.getTemperatureMaxC() != null
                    && day.getTemperatureMaxC() > stage.getMaxTemperatureC()) {
                alerts.add(new WeatherAlert("HIGH_TEMPERATURE", "warning", "⚠ High Temperature Alert",
                        "Forecast maximum temperature exceeds the configured prototype threshold for the "
                                + stage.getName() + " stage.", date));
                highTempAdded = true;
            }
            if (!lowTempAdded && day.getTemperatureMinC() != null
                    && day.getTemperatureMinC() < stage.getMinTemperatureC()) {
                alerts.add(new WeatherAlert("LOW_TEMPERATURE", "warning", "❄ Low Temperature Alert",
                        "Forecast minimum temperature is below the configured prototype threshold for the "
                                + stage.getName() + " stage.", date));
                lowTempAdded = true;
            }
            if (!rainAdded && day.getPrecipitationMm() != null
                    && day.getPrecipitationMm() >= stage.getRainfallThresholdMm()) {
                alerts.add(new WeatherAlert("HEAVY_RAIN", "danger", "☔ Heavy Rain Alert",
                        "Forecast rainfall exceeds the configured prototype threshold. Check field drainage and avoid risky field operations.", date));
                rainAdded = true;
            }
            if (!humidityAdded && day.getHumidityMeanPct() != null
                    && (day.getHumidityMeanPct() < stage.getMinHumidityPct()
                    || day.getHumidityMeanPct() > stage.getMaxHumidityPct())) {
                alerts.add(new WeatherAlert("HUMIDITY", "warning", "💧 Humidity Advisory",
                        "Forecast mean humidity is outside the configured prototype range for the " + stage.getName() + " stage.", date));
                humidityAdded = true;
            }
            if (!windAdded && day.getWindSpeedMaxKmh() != null
                    && day.getWindSpeedMaxKmh() >= advisoryThresholds.getStrongWindKmh()) {
                alerts.add(new WeatherAlert("WIND", "warning", "🌬 Spraying Advisory",
                        "High wind is expected. Consider postponing spraying to reduce spray drift.", date));
                windAdded = true;
            }
            if (!irrigationAdded && day.getPrecipitationMm() != null
                    && day.getPrecipitationMm() >= advisoryThresholds.getRainWarningMm()) {
                alerts.add(new WeatherAlert("IRRIGATION", "info", "💧 Irrigation Advisory",
                        "Rainfall is expected. Consider checking field moisture before irrigation.", date));
                irrigationAdded = true;
            }
        }

        if (alerts.isEmpty()) {
            alerts.add(new WeatherAlert("NORMAL", "success", "🌱 Normal Conditions",
                    "Forecast conditions are within the configured prototype range for the estimated crop stage.",
                    forecastRows.get(0).getDate() != null ? forecastRows.get(0).getDate().format(DISPLAY) : null));
        }
        return alerts;
    }

    private String buildDailyAdvisory(DailyWeather day,
                                      CropWeatherProfileRegistry.CropWeatherProfile profile) {
        if (day == null) return "Weather data unavailable.";
        CropWeatherProfileRegistry.StageConfig stage = day.getCropStage() != null
                ? profile.stageByName(day.getCropStage())
                : profile.getStages().get(0);
        List<String> messages = new ArrayList<>();

        if (day.getTemperatureMaxC() != null && day.getTemperatureMaxC() > stage.getMaxTemperatureC()) {
            messages.add("High temperature: monitor crop stress and soil moisture");
        }
        if (day.getTemperatureMinC() != null && day.getTemperatureMinC() < stage.getMinTemperatureC()) {
            messages.add("Low temperature below configured stage threshold");
        }
        if (day.getPrecipitationMm() != null && day.getPrecipitationMm() >= stage.getRainfallThresholdMm()) {
            messages.add("High rainfall: check drainage and postpone unsuitable field work");
        } else if (day.getPrecipitationMm() != null && day.getPrecipitationMm() >= advisoryThresholds.getRainWarningMm()) {
            messages.add("Rain expected: check field moisture before irrigation");
        }
        if (day.getWindSpeedMaxKmh() != null && day.getWindSpeedMaxKmh() >= advisoryThresholds.getStrongWindKmh()) {
            messages.add("High wind: avoid spraying");
        }
        if (day.getHumidityMeanPct() != null && (day.getHumidityMeanPct() < stage.getMinHumidityPct()
                || day.getHumidityMeanPct() > stage.getMaxHumidityPct())) {
            messages.add("Humidity outside configured stage range");
        }
        if (messages.isEmpty()) messages.add("Conditions within configured prototype range");
        return String.join("; ", messages) + ".";
    }

    private WaterStatus buildWaterStatus(List<DailyWeather> rows, LocalDate sowingDate, LocalDate today) {
        if (rows == null || rows.isEmpty() || sowingDate.isAfter(today)) return null;
        LocalDate start = today.minusDays(6);
        if (start.isBefore(sowingDate)) start = sowingDate;

        double rainfall = 0.0;
        double demand = 0.0;
        boolean hasEt0 = false;
        boolean hasAny = false;
        for (DailyWeather row : rows) {
            if (row.getDate() == null || row.getDate().isBefore(start) || row.getDate().isAfter(today)) continue;
            hasAny = true;
            rainfall += safe(row.getPrecipitationMm());
            if (row.getEt0Mm() != null) {
                demand += row.getEt0Mm();
                hasEt0 = true;
            }
        }
        if (!hasAny) return null;

        WaterStatus status = new WaterStatus();
        status.setPeriod(start.format(DISPLAY) + " – " + today.format(DISPLAY));
        status.setRainfallReceived(round1(rainfall));
        if (hasEt0) {
            double deficit = Math.max(0.0, demand - rainfall);
            status.setEstimatedWaterDemand(round1(demand));
            status.setWaterDeficit(round1(deficit));
            status.setStatus(deficit <= 0.0 ? "Adequate by prototype rainfall-vs-ET0 check" : "Estimated deficit");
        } else {
            status.setStatus("ET0 unavailable");
        }
        status.setMethodology("Prototype Estimated Water Requirement = sum of Open-Meteo FAO reference ET0 for the period. Water Deficit = max(ET0 - rainfall, 0). This is not crop-specific ETc and not a field soil-moisture measurement.");
        return status;
    }

    private List<StageWaterStatus> buildStageWaterTable(List<DailyWeather> rows,
                                                         LocalDate sowingDate, LocalDate today) {
        Map<String, StageAccumulator> byStage = new LinkedHashMap<>();
        if (rows == null) return new ArrayList<>();

        for (DailyWeather row : rows) {
            if (row.getDate() == null || row.getDate().isBefore(sowingDate) || row.getDate().isAfter(today)
                    || row.getCropStage() == null) continue;
            StageAccumulator acc = byStage.computeIfAbsent(row.getCropStage(), k -> new StageAccumulator());
            if (acc.start == null || row.getDate().isBefore(acc.start)) acc.start = row.getDate();
            if (acc.end == null || row.getDate().isAfter(acc.end)) acc.end = row.getDate();
            acc.rainfall += safe(row.getPrecipitationMm());
            if (row.getEt0Mm() != null) {
                acc.demand += row.getEt0Mm();
                acc.hasEt0 = true;
            }
        }

        List<StageWaterStatus> result = new ArrayList<>();
        for (Map.Entry<String, StageAccumulator> entry : byStage.entrySet()) {
            StageAccumulator acc = entry.getValue();
            StageWaterStatus row = new StageWaterStatus();
            row.setCropStage(entry.getKey());
            row.setPeriod(acc.start.format(DISPLAY) + " – " + acc.end.format(DISPLAY));
            row.setRainfallReceived(round1(acc.rainfall));
            if (acc.hasEt0) {
                double deficit = Math.max(0.0, acc.demand - acc.rainfall);
                row.setEstimatedWaterDemand(round1(acc.demand));
                row.setWaterDeficit(round1(deficit));
                row.setStatus(deficit <= 0.0 ? "Adequate" : "Estimated Deficit");
            } else {
                row.setStatus("ET0 unavailable");
            }
            result.add(row);
        }
        return result;
    }

    private CropCalendarActivity buildActivity(CropCalendarTemplateProvider.ActivityTemplate at,
                                                LocalDate sowingDate, LocalDate today,
                                                Map<LocalDate, DailyWeather> timeline,
                                                boolean weatherAvailable, LocalDate forecastHorizon,
                                                String irrigationSource,
                                                CropWeatherProfileRegistry.CropWeatherProfile profile) {
        LocalDate plannedDate = sowingDate.plusDays(at.dayAfterSowingMin);
        CropCalendarActivity activity = new CropCalendarActivity();
        activity.setActivityName(at.activityName);
        activity.setActivityType(at.activityType);
        activity.setDayAfterSowingLabel(at.dayAfterSowingMin == at.dayAfterSowingMax
                ? "Day " + at.dayAfterSowingMin
                : "Day " + at.dayAfterSowingMin + "-" + at.dayAfterSowingMax);
        activity.setPlannedDate(plannedDate.format(DISPLAY));
        activity.setWeatherSensitive(at.weatherSensitive);
        activity.setGuidance(at.guidance);

        DailyWeather day = timeline.get(plannedDate);
        if (day != null && day.getCropStage() != null) {
            activity.setCropStage(day.getCropStage());
        } else if (forecastHorizon != null && plannedDate.isAfter(forecastHorizon)) {
            activity.setCropStage("Stage check available closer to date");
        } else if (!weatherAvailable) {
            activity.setCropStage("Estimated stage unavailable without weather data");
        } else {
            activity.setCropStage("Stage data unavailable for this date");
        }

        if (plannedDate.isBefore(today)) {
            activity.setWeatherStatus("PAST");
            activity.setStatusColor("blue");
            activity.setWeatherMessage(day != null ? "Historical weather data recorded for this date." : "Past activity; exact historical weather row not available.");
            return activity;
        }

        if (!at.weatherSensitive) {
            activity.setWeatherStatus("NOT_APPLICABLE");
            activity.setStatusColor("blue");
            return activity;
        }

        if (!weatherAvailable) {
            activity.setWeatherStatus("NO_LOCATION");
            activity.setWeatherMessage("Add a valid farm location to enable real weather checks for this activity.");
            activity.setStatusColor("blue");
            return activity;
        }

        if (forecastHorizon == null || plannedDate.isAfter(forecastHorizon)) {
            activity.setWeatherStatus("FORECAST_UNAVAILABLE");
            activity.setWeatherMessage("Weather check available closer to this activity date.");
            activity.setStatusColor("blue");
            return activity;
        }

        if (day == null || !"FORECAST".equals(day.getDataType())) {
            activity.setWeatherStatus("FORECAST_UNAVAILABLE");
            activity.setWeatherMessage("Forecast weather data is not available for this exact date.");
            activity.setStatusColor("blue");
            return activity;
        }

        evaluateActivityWeather(activity, at, day, timeline, forecastHorizon, irrigationSource, profile);
        return activity;
    }

    private void evaluateActivityWeather(CropCalendarActivity activity,
                                         CropCalendarTemplateProvider.ActivityTemplate at,
                                         DailyWeather day,
                                         Map<LocalDate, DailyWeather> timeline,
                                         LocalDate forecastHorizon,
                                         String irrigationSource,
                                         CropWeatherProfileRegistry.CropWeatherProfile profile) {
        CropWeatherProfileRegistry.StageConfig stage = day.getCropStage() != null
                ? profile.stageByName(day.getCropStage()) : profile.getStages().get(0);
        double rain = safe(day.getPrecipitationMm());
        double wind = safe(day.getWindSpeedMaxKmh());
        Double maxTemp = day.getTemperatureMaxC();

        String status = "NORMAL";
        String color = "green";
        List<String> messages = new ArrayList<>();
        boolean needsReschedule = false;

        if ("SPRAYING".equals(at.activityType)) {
            if (rain >= advisoryThresholds.getRainWarningMm()) {
                status = "ALERT";
                color = "red";
                messages.add("Rain is expected; consider postponing spraying");
                needsReschedule = true;
            }
            if (wind >= advisoryThresholds.getStrongWindKmh()) {
                status = "ALERT";
                color = "red";
                messages.add("High wind may cause spray drift; consider postponing spraying");
                needsReschedule = true;
            }
        } else if ("IRRIGATION".equals(at.activityType)) {
            if (rain >= stage.getRainfallThresholdMm()) {
                status = "ADVISORY";
                color = "orange";
                messages.add("High rainfall is forecast; check drainage and field moisture before irrigation");
            } else if (rain >= advisoryThresholds.getRainWarningMm()) {
                status = "ADVISORY";
                color = "orange";
                messages.add("Rain is forecast; consider postponing irrigation after checking field moisture");
            } else {
                messages.add("No significant rainfall warning for the planned date; check field moisture before irrigating");
            }
            if (irrigationSource != null && !irrigationSource.isBlank()) {
                messages.add("Selected irrigation source: " + irrigationSource);
            }
        } else if ("FERTILIZER".equals(at.activityType)) {
            if (rain >= advisoryThresholds.getRainWarningMm()) {
                status = "ADVISORY";
                color = "orange";
                messages.add("Rain is expected; time fertilizer application carefully to reduce nutrient loss");
            } else {
                messages.add("No significant rainfall warning for the planned date");
            }
        }

        if (maxTemp != null && maxTemp > stage.getMaxTemperatureC()) {
            if ("NORMAL".equals(status)) {
                status = "ADVISORY";
                color = "orange";
            }
            messages.add("Maximum temperature exceeds the configured prototype threshold for " + stage.getName());
        }

        if (messages.isEmpty()) messages.add("Forecast conditions are within configured prototype limits for this activity");
        activity.setWeatherStatus(status);
        activity.setWeatherMessage(String.join(". ", messages) + ".");
        activity.setStatusColor(color);

        if (needsReschedule) {
            Optional<DailyWeather> better = findBetterForecastDay(timeline, day.getDate(), forecastHorizon, at.activityType);
            if (better.isPresent()) {
                activity.setSuggestedDate(better.get().getDate().format(DISPLAY));
                activity.setRescheduleReason("A later forecast day has lower rain/wind risk within the configured look-ahead window.");
            } else {
                activity.setRescheduleReason("No clearly better forecast day was found in the configured look-ahead window. Check again closer to the activity date.");
            }
        }
    }

    private Optional<DailyWeather> findBetterForecastDay(Map<LocalDate, DailyWeather> timeline,
                                                          LocalDate plannedDate,
                                                          LocalDate forecastHorizon,
                                                          String activityType) {
        LocalDate end = plannedDate.plusDays(advisoryThresholds.getRescheduleLookaheadDays());
        if (forecastHorizon != null && end.isAfter(forecastHorizon)) end = forecastHorizon;
        for (LocalDate d = plannedDate.plusDays(1); !d.isAfter(end); d = d.plusDays(1)) {
            DailyWeather day = timeline.get(d);
            if (day == null || !"FORECAST".equals(day.getDataType())) continue;
            boolean rainOk = safe(day.getPrecipitationMm()) < advisoryThresholds.getRainWarningMm();
            boolean windOk = safe(day.getWindSpeedMaxKmh()) < advisoryThresholds.getStrongWindKmh();
            if ("SPRAYING".equals(activityType) && rainOk && windOk) return Optional.of(day);
        }
        return Optional.empty();
    }

    private CropCalendarActivity buildHarvestActivity(int[] durationRange, LocalDate harvestStart, LocalDate harvestEnd) {
        CropCalendarActivity harvest = new CropCalendarActivity();
        harvest.setActivityName("Expected Harvest Window");
        harvest.setActivityType("HARVEST");
        harvest.setDayAfterSowingLabel("Day " + durationRange[0] + "-" + durationRange[1]);
        harvest.setPlannedDate(harvestStart.format(DISPLAY) + " - " + harvestEnd.format(DISPLAY));
        harvest.setCropStage("Maturity / harvest readiness check");
        harvest.setWeatherSensitive(false);
        harvest.setWeatherStatus("NOT_APPLICABLE");
        harvest.setGuidance("Confirm harvest using crop maturity signs and local recommendations, not calendar date alone.");
        harvest.setStatusColor("blue");
        return harvest;
    }

    private Map<LocalDate, DailyWeather> indexByDate(List<DailyWeather> rows) {
        Map<LocalDate, DailyWeather> map = new LinkedHashMap<>();
        if (rows != null) {
            for (DailyWeather row : rows) if (row.getDate() != null) map.put(row.getDate(), row);
        }
        return map;
    }

    private String buildLocationQuery(CropCalendarRequest request) {
        List<String> parts = new ArrayList<>();
        addDistinct(parts, request.getLocation());
        addDistinct(parts, request.getDistrict());
        addDistinct(parts, request.getState());
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    private void addDistinct(List<String> parts, String value) {
        if (value == null || value.trim().isEmpty()) return;
        String cleaned = value.trim();
        for (String existing : parts) {
            if (existing.equalsIgnoreCase(cleaned)) return;
        }
        parts.add(cleaned);
    }

    private String normalizeDurationProfile(String value) {
        if (value == null || value.isBlank()) return "Medium Duration";
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (v.startsWith("short")) return "Short Duration";
        if (v.startsWith("long")) return "Long Duration";
        return "Medium Duration";
    }

    private int[] selectDurationRange(int min, int max, String profile) {
        if (min >= max) return new int[]{min, max};
        int mid = (int) Math.round((min + max) / 2.0);
        int halfWindow = Math.max(3, (max - min) / 8);
        if ("Short Duration".equals(profile)) {
            return new int[]{min, Math.min(max, min + Math.max(5, halfWindow))};
        }
        if ("Long Duration".equals(profile)) {
            return new int[]{Math.max(min, max - Math.max(5, halfWindow)), max};
        }
        return new int[]{Math.max(min, mid - halfWindow), Math.min(max, mid + halfWindow)};
    }

    private String formatDuration(int[] range, String profile) {
        String days = range[0] == range[1] ? range[0] + " days" : range[0] + "–" + range[1] + " days";
        return days + " (" + profile + " profile within the crop's configured duration range)";
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private LocalDate parseDate(String sowingDate) {
        try {
            return LocalDate.parse(sowingDate.trim(), ISO);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid sowing date format. Expected yyyy-MM-dd, got: " + sowingDate);
        }
    }

    private double safe(Double value) { return value != null ? value : 0.0; }
    private Double roundNullable(Double value) { return value == null ? null : round1(value); }
    private Double round1(double value) { return Math.round(value * 10.0) / 10.0; }

    private static class StageAccumulator {
        private LocalDate start;
        private LocalDate end;
        private double rainfall;
        private double demand;
        private boolean hasEt0;
    }
}
