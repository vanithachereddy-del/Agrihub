package com.agrihub.model;

public class WeatherThresholdInfo {
    private String stage;
    private Double maxTemperatureC;
    private Double minTemperatureC;
    private Double rainfallThresholdMm;
    private Double maxHumidityPct;
    private Double minHumidityPct;
    private String validationNote;

    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public Double getMaxTemperatureC() { return maxTemperatureC; }
    public void setMaxTemperatureC(Double maxTemperatureC) { this.maxTemperatureC = maxTemperatureC; }
    public Double getMinTemperatureC() { return minTemperatureC; }
    public void setMinTemperatureC(Double minTemperatureC) { this.minTemperatureC = minTemperatureC; }
    public Double getRainfallThresholdMm() { return rainfallThresholdMm; }
    public void setRainfallThresholdMm(Double rainfallThresholdMm) { this.rainfallThresholdMm = rainfallThresholdMm; }
    public Double getMaxHumidityPct() { return maxHumidityPct; }
    public void setMaxHumidityPct(Double maxHumidityPct) { this.maxHumidityPct = maxHumidityPct; }
    public Double getMinHumidityPct() { return minHumidityPct; }
    public void setMinHumidityPct(Double minHumidityPct) { this.minHumidityPct = minHumidityPct; }
    public String getValidationNote() { return validationNote; }
    public void setValidationNote(String validationNote) { this.validationNote = validationNote; }
}
