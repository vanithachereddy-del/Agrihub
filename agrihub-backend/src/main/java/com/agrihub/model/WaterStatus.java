package com.agrihub.model;

public class WaterStatus {
    private String period;
    private Double rainfallReceived;
    private Double estimatedWaterDemand;
    private Double waterDeficit;
    private String status;
    private String methodology;

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public Double getRainfallReceived() { return rainfallReceived; }
    public void setRainfallReceived(Double rainfallReceived) { this.rainfallReceived = rainfallReceived; }
    public Double getEstimatedWaterDemand() { return estimatedWaterDemand; }
    public void setEstimatedWaterDemand(Double estimatedWaterDemand) { this.estimatedWaterDemand = estimatedWaterDemand; }
    public Double getWaterDeficit() { return waterDeficit; }
    public void setWaterDeficit(Double waterDeficit) { this.waterDeficit = waterDeficit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMethodology() { return methodology; }
    public void setMethodology(String methodology) { this.methodology = methodology; }
}
