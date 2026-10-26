package com.agrihub.model;

public class WeatherAlert {
    private String type;
    private String severity;
    private String title;
    private String message;
    private String date;

    public WeatherAlert() {}

    public WeatherAlert(String type, String severity, String title, String message, String date) {
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
