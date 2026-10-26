package com.agrihub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A single crop's result within a Crop Recommendation response.
 * <p>
 * IMPORTANT: {@code suitabilityScore} is a RULE-BASED SUITABILITY SCORE
 * (weighted match against soil/season/water/pH), NOT a machine-learning
 * "accuracy" figure. No trained/evaluated model backs this number yet.
 */
public class CropSuitabilityResult {

    private String cropId;
    private String cropName;
    private String category;
    private double suitabilityScore;
    private List<String> matchedFactors = new ArrayList<>();
    private List<String> notes = new ArrayList<>();
    private String reason;
    private String waterRequirement;
    private String expectedDuration;

    public CropSuitabilityResult() {
    }

    public String getCropId() {
        return cropId;
    }

    public void setCropId(String cropId) {
        this.cropId = cropId;
    }

    public String getCropName() {
        return cropName;
    }

    public void setCropName(String cropName) {
        this.cropName = cropName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSuitabilityScore() {
        return suitabilityScore;
    }

    public void setSuitabilityScore(double suitabilityScore) {
        this.suitabilityScore = suitabilityScore;
    }

    public List<String> getMatchedFactors() {
        return matchedFactors;
    }

    public void setMatchedFactors(List<String> matchedFactors) {
        this.matchedFactors = matchedFactors;
    }

    public List<String> getNotes() {
        return notes;
    }

    public void setNotes(List<String> notes) {
        this.notes = notes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getWaterRequirement() {
        return waterRequirement;
    }

    public void setWaterRequirement(String waterRequirement) {
        this.waterRequirement = waterRequirement;
    }

    public String getExpectedDuration() {
        return expectedDuration;
    }

    public void setExpectedDuration(String expectedDuration) {
        this.expectedDuration = expectedDuration;
    }
}
