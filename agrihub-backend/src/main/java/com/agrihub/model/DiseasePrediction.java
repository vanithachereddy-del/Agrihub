package com.agrihub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Response payload for the placeholder disease detection service.
 * Replace {@link com.agrihub.service.DiseaseDetectionService} with a real ML model later.
 */
public class DiseasePrediction {

    private String diseaseName;
    private double confidence;
    private String description;
    private List<String> recommendedFertilizers = new ArrayList<>();
    private List<String> recommendedPesticides = new ArrayList<>();
    private List<String> preventionTips = new ArrayList<>();
    private List<String> symptoms = new ArrayList<>();
    private boolean placeholder = true;

    public DiseasePrediction() {
    }

    public DiseasePrediction(String diseaseName, double confidence, String description,
                             List<String> recommendedFertilizers, List<String> recommendedPesticides,
                             List<String> preventionTips) {
        this.diseaseName = diseaseName;
        this.confidence = confidence;
        this.description = description;
        this.recommendedFertilizers = recommendedFertilizers;
        this.recommendedPesticides = recommendedPesticides;
        this.preventionTips = preventionTips;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRecommendedFertilizers() {
        return recommendedFertilizers;
    }

    public void setRecommendedFertilizers(List<String> recommendedFertilizers) {
        this.recommendedFertilizers = recommendedFertilizers;
    }

    public List<String> getRecommendedPesticides() {
        return recommendedPesticides;
    }

    public void setRecommendedPesticides(List<String> recommendedPesticides) {
        this.recommendedPesticides = recommendedPesticides;
    }

    public List<String> getPreventionTips() {
        return preventionTips;
    }

    public void setPreventionTips(List<String> preventionTips) {
        this.preventionTips = preventionTips;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }
}
