package com.agrihub.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "crops")
public class CropGuide {

    @Id
    private String id;
    private String name;
    private String category;
    private String description;
    private String growthStages;
    private String fertilizerSchedule;
    private String irrigationSchedule;
    private String harvestDuration;
    private String suitableSoil;
    private String bestSeason;
    private List<String> diseases = new ArrayList<>();
    private String imageUrl;

    // ---- Added for Crop Recommendation module (Phase 1) ----
    // Structured, matchable versions of the free-text fields above.
    // Values seeded for the initial 6 crops are general agronomy reference
    // ranges and MUST be validated against a proper agricultural dataset
    // or expert source before being treated as authoritative.
    private List<String> soilTypes = new ArrayList<>();
    private List<String> seasons = new ArrayList<>();
    /** One of: Low, Medium, High */
    private String waterRequirement;
    private Double phMin;
    private Double phMax;
    // General reference list of irrigation methods this crop is commonly
    // grown with in Indian farming practice. This is NOT a validated
    // per-crop irrigation trial dataset - it should be checked against
    // local agricultural extension guidance before being treated as fact.
    private List<String> irrigationSources = new ArrayList<>();

    public CropGuide() {
    }

    public CropGuide(String name, String category, String description, String growthStages,
                     String fertilizerSchedule, String irrigationSchedule, String harvestDuration,
                     String suitableSoil, String bestSeason, List<String> diseases, String imageUrl) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.growthStages = growthStages;
        this.fertilizerSchedule = fertilizerSchedule;
        this.irrigationSchedule = irrigationSchedule;
        this.harvestDuration = harvestDuration;
        this.suitableSoil = suitableSoil;
        this.bestSeason = bestSeason;
        this.diseases = diseases != null ? diseases : new ArrayList<>();
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGrowthStages() {
        return growthStages;
    }

    public void setGrowthStages(String growthStages) {
        this.growthStages = growthStages;
    }

    public String getFertilizerSchedule() {
        return fertilizerSchedule;
    }

    public void setFertilizerSchedule(String fertilizerSchedule) {
        this.fertilizerSchedule = fertilizerSchedule;
    }

    public String getIrrigationSchedule() {
        return irrigationSchedule;
    }

    public void setIrrigationSchedule(String irrigationSchedule) {
        this.irrigationSchedule = irrigationSchedule;
    }

    public String getHarvestDuration() {
        return harvestDuration;
    }

    public void setHarvestDuration(String harvestDuration) {
        this.harvestDuration = harvestDuration;
    }

    public String getSuitableSoil() {
        return suitableSoil;
    }

    public void setSuitableSoil(String suitableSoil) {
        this.suitableSoil = suitableSoil;
    }

    public String getBestSeason() {
        return bestSeason;
    }

    public void setBestSeason(String bestSeason) {
        this.bestSeason = bestSeason;
    }

    public List<String> getDiseases() {
        return diseases;
    }

    public void setDiseases(List<String> diseases) {
        this.diseases = diseases;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getSoilTypes() {
        return soilTypes;
    }

    public void setSoilTypes(List<String> soilTypes) {
        this.soilTypes = soilTypes != null ? soilTypes : new ArrayList<>();
    }

    public List<String> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<String> seasons) {
        this.seasons = seasons != null ? seasons : new ArrayList<>();
    }

    public String getWaterRequirement() {
        return waterRequirement;
    }

    public void setWaterRequirement(String waterRequirement) {
        this.waterRequirement = waterRequirement;
    }

    public Double getPhMin() {
        return phMin;
    }

    public void setPhMin(Double phMin) {
        this.phMin = phMin;
    }

    public Double getPhMax() {
        return phMax;
    }

    public void setPhMax(Double phMax) {
        this.phMax = phMax;
    }

    public List<String> getIrrigationSources() {
        return irrigationSources;
    }

    public void setIrrigationSources(List<String> irrigationSources) {
        this.irrigationSources = irrigationSources != null ? irrigationSources : new ArrayList<>();
    }
}
