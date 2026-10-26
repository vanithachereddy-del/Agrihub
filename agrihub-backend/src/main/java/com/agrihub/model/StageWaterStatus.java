package com.agrihub.model;

public class StageWaterStatus {
    private String cropStage;
    private String period;
    private Double estimatedWaterDemand;
    private Double rainfallReceived;
    private Double waterDeficit;
    private String status;

    public String getCropStage() { return cropStage; }
    public void setCropStage(String cropStage) { this.cropStage = cropStage; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Double getEstimatedWaterDemand() { return estimatedWaterDemand; }
    public void setEstimatedWaterDemand(Double estimatedWaterDemand) { this.estimatedWaterDemand = estimatedWaterDemand; }
    public Double getRainfallReceived() { return rainfallReceived; }
    public void setRainfallReceived(Double rainfallReceived) { this.rainfallReceived = rainfallReceived; }
    public Double getWaterDeficit() { return waterDeficit; }
    public void setWaterDeficit(Double waterDeficit) { this.waterDeficit = waterDeficit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
