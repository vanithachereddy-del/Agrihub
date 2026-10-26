package com.agrihub.model;

import java.util.ArrayList;
import java.util.List;

public class CropCalendarResponse {

    private String crop;
    private String cropVarietyDuration;
    private String season;
    private String sowingDate;
    private String durationLabel;
    private String harvestStart;
    private String harvestEnd;
    private String queriedLocation;
    private String resolvedLocation;
    private boolean weatherAvailable;
    private String forecastAvailableUntil;

    private long daysAfterSowing;
    private String currentStage;
    private Double baseTemperature;
    private String baseTemperatureSourceNote;
    private Double accumulatedGdd;

    private CropWeatherSummary currentWeatherSummary;
    private List<DailyWeather> dailyForecast = new ArrayList<>();
    private WeatherThresholdInfo currentStageThresholds;
    private List<WeatherAlert> weatherAlerts = new ArrayList<>();
    private WaterStatus waterStatus;
    private List<StageWaterStatus> stageWaterTable = new ArrayList<>();

    private List<CropCalendarActivity> activities = new ArrayList<>();
    private String disclaimer;

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }
    public String getCropVarietyDuration() { return cropVarietyDuration; }
    public void setCropVarietyDuration(String cropVarietyDuration) { this.cropVarietyDuration = cropVarietyDuration; }
    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }
    public String getSowingDate() { return sowingDate; }
    public void setSowingDate(String sowingDate) { this.sowingDate = sowingDate; }
    public String getDurationLabel() { return durationLabel; }
    public void setDurationLabel(String durationLabel) { this.durationLabel = durationLabel; }
    public String getHarvestStart() { return harvestStart; }
    public void setHarvestStart(String harvestStart) { this.harvestStart = harvestStart; }
    public String getHarvestEnd() { return harvestEnd; }
    public void setHarvestEnd(String harvestEnd) { this.harvestEnd = harvestEnd; }
    public String getQueriedLocation() { return queriedLocation; }
    public void setQueriedLocation(String queriedLocation) { this.queriedLocation = queriedLocation; }
    public String getResolvedLocation() { return resolvedLocation; }
    public void setResolvedLocation(String resolvedLocation) { this.resolvedLocation = resolvedLocation; }
    public boolean isWeatherAvailable() { return weatherAvailable; }
    public void setWeatherAvailable(boolean weatherAvailable) { this.weatherAvailable = weatherAvailable; }
    public String getForecastAvailableUntil() { return forecastAvailableUntil; }
    public void setForecastAvailableUntil(String forecastAvailableUntil) { this.forecastAvailableUntil = forecastAvailableUntil; }
    public long getDaysAfterSowing() { return daysAfterSowing; }
    public void setDaysAfterSowing(long daysAfterSowing) { this.daysAfterSowing = daysAfterSowing; }
    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
    public Double getBaseTemperature() { return baseTemperature; }
    public void setBaseTemperature(Double baseTemperature) { this.baseTemperature = baseTemperature; }
    public String getBaseTemperatureSourceNote() { return baseTemperatureSourceNote; }
    public void setBaseTemperatureSourceNote(String baseTemperatureSourceNote) { this.baseTemperatureSourceNote = baseTemperatureSourceNote; }
    public Double getAccumulatedGdd() { return accumulatedGdd; }
    public void setAccumulatedGdd(Double accumulatedGdd) { this.accumulatedGdd = accumulatedGdd; }
    public CropWeatherSummary getCurrentWeatherSummary() { return currentWeatherSummary; }
    public void setCurrentWeatherSummary(CropWeatherSummary currentWeatherSummary) { this.currentWeatherSummary = currentWeatherSummary; }
    public List<DailyWeather> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyWeather> dailyForecast) { this.dailyForecast = dailyForecast; }
    public WeatherThresholdInfo getCurrentStageThresholds() { return currentStageThresholds; }
    public void setCurrentStageThresholds(WeatherThresholdInfo currentStageThresholds) { this.currentStageThresholds = currentStageThresholds; }
    public List<WeatherAlert> getWeatherAlerts() { return weatherAlerts; }
    public void setWeatherAlerts(List<WeatherAlert> weatherAlerts) { this.weatherAlerts = weatherAlerts; }
    public WaterStatus getWaterStatus() { return waterStatus; }
    public void setWaterStatus(WaterStatus waterStatus) { this.waterStatus = waterStatus; }
    public List<StageWaterStatus> getStageWaterTable() { return stageWaterTable; }
    public void setStageWaterTable(List<StageWaterStatus> stageWaterTable) { this.stageWaterTable = stageWaterTable; }
    public List<CropCalendarActivity> getActivities() { return activities; }
    public void setActivities(List<CropCalendarActivity> activities) { this.activities = activities; }
    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }
}
