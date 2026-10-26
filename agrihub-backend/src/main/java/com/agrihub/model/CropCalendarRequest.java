package com.agrihub.model;

/**
 * Input for POST /crop-calendar/generate.
 * crop and sowingDate (ISO yyyy-MM-dd) are required.
 * Location fields are optional so the calendar can still be generated when
 * weather data is unavailable.
 */
public class CropCalendarRequest {

    private String crop;
    private String cropVarietyDuration;
    private String sowingDate;
    private String season;
    private String state;
    private String district;
    private String location;
    private String irrigationSource;

    public CropCalendarRequest() {
    }

    public String getCrop() { return crop; }
    public void setCrop(String crop) { this.crop = crop; }

    public String getCropVarietyDuration() { return cropVarietyDuration; }
    public void setCropVarietyDuration(String cropVarietyDuration) { this.cropVarietyDuration = cropVarietyDuration; }

    public String getSowingDate() { return sowingDate; }
    public void setSowingDate(String sowingDate) { this.sowingDate = sowingDate; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getIrrigationSource() { return irrigationSource; }
    public void setIrrigationSource(String irrigationSource) { this.irrigationSource = irrigationSource; }
}
