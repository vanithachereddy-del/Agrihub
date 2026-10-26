package com.agrihub.model;

/**
 * One activity row in a generated Crop Calendar.
 * <p>
 * weatherStatus is one of: PAST, NOT_APPLICABLE, NO_LOCATION,
 * FORECAST_UNAVAILABLE, NORMAL, ADVISORY, ALERT.
 * statusColor is one of: green, orange, red, blue (see AgriHub UI legend).
 */
public class CropCalendarActivity {

    private String activityName;
    private String activityType;
    private String dayAfterSowingLabel;
    private String plannedDate;
    private String cropStage;
    private boolean weatherSensitive;
    private String weatherStatus;
    private String weatherMessage;
    private String suggestedDate;
    private String rescheduleReason;
    private String guidance;
    private String statusColor;

    public CropCalendarActivity() {
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDayAfterSowingLabel() {
        return dayAfterSowingLabel;
    }

    public void setDayAfterSowingLabel(String dayAfterSowingLabel) {
        this.dayAfterSowingLabel = dayAfterSowingLabel;
    }

    public String getPlannedDate() {
        return plannedDate;
    }

    public void setPlannedDate(String plannedDate) {
        this.plannedDate = plannedDate;
    }

    public String getCropStage() { return cropStage; }

    public void setCropStage(String cropStage) { this.cropStage = cropStage; }

    public boolean isWeatherSensitive() {
        return weatherSensitive;
    }

    public void setWeatherSensitive(boolean weatherSensitive) {
        this.weatherSensitive = weatherSensitive;
    }

    public String getWeatherStatus() {
        return weatherStatus;
    }

    public void setWeatherStatus(String weatherStatus) {
        this.weatherStatus = weatherStatus;
    }

    public String getWeatherMessage() {
        return weatherMessage;
    }

    public void setWeatherMessage(String weatherMessage) {
        this.weatherMessage = weatherMessage;
    }

    public String getSuggestedDate() {
        return suggestedDate;
    }

    public void setSuggestedDate(String suggestedDate) {
        this.suggestedDate = suggestedDate;
    }

    public String getRescheduleReason() {
        return rescheduleReason;
    }

    public void setRescheduleReason(String rescheduleReason) {
        this.rescheduleReason = rescheduleReason;
    }

    public String getGuidance() {
        return guidance;
    }

    public void setGuidance(String guidance) {
        this.guidance = guidance;
    }

    public String getStatusColor() {
        return statusColor;
    }

    public void setStatusColor(String statusColor) {
        this.statusColor = statusColor;
    }
}
