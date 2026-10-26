package com.agrihub.model;

import java.time.LocalDate;

/**
 * One day of real Open-Meteo weather data used by the Dynamic Crop Weather
 * Calendar. Derived GDD/stage/advisory values are filled by CropCalendarService.
 */
public class DailyWeather {

    private LocalDate date;
    private Double precipitationMm;
    private Double windSpeedMaxKmh;
    private Double temperatureMaxC;
    private Double temperatureMinC;
    private Double humidityMeanPct;
    private Double et0Mm;
    private Double dailyGdd;
    private Double cumulativeGdd;
    private String cropStage;
    private String advisory;
    private String dataType;

    public DailyWeather() {
    }

    public DailyWeather(LocalDate date, Double precipitationMm, Double windSpeedMaxKmh, Double temperatureMaxC) {
        this.date = date;
        this.precipitationMm = precipitationMm;
        this.windSpeedMaxKmh = windSpeedMaxKmh;
        this.temperatureMaxC = temperatureMaxC;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getPrecipitationMm() { return precipitationMm; }
    public void setPrecipitationMm(Double precipitationMm) { this.precipitationMm = precipitationMm; }

    public Double getWindSpeedMaxKmh() { return windSpeedMaxKmh; }
    public void setWindSpeedMaxKmh(Double windSpeedMaxKmh) { this.windSpeedMaxKmh = windSpeedMaxKmh; }

    public Double getTemperatureMaxC() { return temperatureMaxC; }
    public void setTemperatureMaxC(Double temperatureMaxC) { this.temperatureMaxC = temperatureMaxC; }

    public Double getTemperatureMinC() { return temperatureMinC; }
    public void setTemperatureMinC(Double temperatureMinC) { this.temperatureMinC = temperatureMinC; }

    public Double getHumidityMeanPct() { return humidityMeanPct; }
    public void setHumidityMeanPct(Double humidityMeanPct) { this.humidityMeanPct = humidityMeanPct; }

    public Double getEt0Mm() { return et0Mm; }
    public void setEt0Mm(Double et0Mm) { this.et0Mm = et0Mm; }

    public Double getDailyGdd() { return dailyGdd; }
    public void setDailyGdd(Double dailyGdd) { this.dailyGdd = dailyGdd; }

    public Double getCumulativeGdd() { return cumulativeGdd; }
    public void setCumulativeGdd(Double cumulativeGdd) { this.cumulativeGdd = cumulativeGdd; }

    public String getCropStage() { return cropStage; }
    public void setCropStage(String cropStage) { this.cropStage = cropStage; }

    public String getAdvisory() { return advisory; }
    public void setAdvisory(String advisory) { this.advisory = advisory; }

    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
}
