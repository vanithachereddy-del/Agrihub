package com.agrihub.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Centralized crop-weather configuration used by the Dynamic Crop Weather
 * Calendar. Nothing in the service layer hardcodes crop base temperatures,
 * GDD stage boundaries, or stage weather thresholds.
 *
 * Base-temperature values are taken from common extension GDD references:
 * - Cotton 15.6 C, Peanut/Groundnut 13.3 C, Corn/Rice 10 C:
 *   UF/IFAS Degree-Days reference table and U.S. extension guidance.
 * - Tomato and Pepper/Chilli 10 C (50 F): Midwestern Regional Climate Center.
 *
 * IMPORTANT: stage GDD boundaries and stage-specific weather thresholds below
 * are PROTOTYPE CONFIGURATION VALUES for this academic decision-support demo.
 * They are NOT official universal agronomic standards. They must be validated
 * for crop variety, location and local extension recommendations before field use.
 */
@Component
public class CropWeatherProfileRegistry {

    public static class StageConfig {
        private final String name;
        private final double startGdd;
        private final double maxTemperatureC;
        private final double minTemperatureC;
        private final double rainfallThresholdMm;
        private final double minHumidityPct;
        private final double maxHumidityPct;

        public StageConfig(String name, double startGdd, double maxTemperatureC, double minTemperatureC,
                           double rainfallThresholdMm, double minHumidityPct, double maxHumidityPct) {
            this.name = name;
            this.startGdd = startGdd;
            this.maxTemperatureC = maxTemperatureC;
            this.minTemperatureC = minTemperatureC;
            this.rainfallThresholdMm = rainfallThresholdMm;
            this.minHumidityPct = minHumidityPct;
            this.maxHumidityPct = maxHumidityPct;
        }

        public String getName() { return name; }
        public double getStartGdd() { return startGdd; }
        public double getMaxTemperatureC() { return maxTemperatureC; }
        public double getMinTemperatureC() { return minTemperatureC; }
        public double getRainfallThresholdMm() { return rainfallThresholdMm; }
        public double getMinHumidityPct() { return minHumidityPct; }
        public double getMaxHumidityPct() { return maxHumidityPct; }
    }

    public static class CropWeatherProfile {
        private final String cropName;
        private final double baseTemperatureC;
        private final String baseTemperatureSourceNote;
        private final List<StageConfig> stages;

        public CropWeatherProfile(String cropName, double baseTemperatureC, String baseTemperatureSourceNote,
                                  List<StageConfig> stages) {
            this.cropName = cropName;
            this.baseTemperatureC = baseTemperatureC;
            this.baseTemperatureSourceNote = baseTemperatureSourceNote;
            this.stages = new ArrayList<>(stages);
            this.stages.sort(Comparator.comparingDouble(StageConfig::getStartGdd));
        }

        public String getCropName() { return cropName; }
        public double getBaseTemperatureC() { return baseTemperatureC; }
        public String getBaseTemperatureSourceNote() { return baseTemperatureSourceNote; }
        public List<StageConfig> getStages() { return new ArrayList<>(stages); }

        public StageConfig stageForGdd(double gdd) {
            StageConfig result = stages.get(0);
            for (StageConfig stage : stages) {
                if (gdd >= stage.getStartGdd()) {
                    result = stage;
                } else {
                    break;
                }
            }
            return result;
        }

        public StageConfig stageByName(String name) {
            if (name == null) return stages.get(0);
            for (StageConfig stage : stages) {
                if (stage.getName().equalsIgnoreCase(name)) return stage;
            }
            return stages.get(0);
        }
    }

    private final Map<String, CropWeatherProfile> profiles = new LinkedHashMap<>();

    public CropWeatherProfileRegistry() {
        register(groundnut());
        register(rice());
        register(cotton());
        register(maize());
        register(tomato());
        register(chilli());
    }

    private void register(CropWeatherProfile profile) {
        profiles.put(profile.getCropName().toLowerCase(Locale.ROOT), profile);
    }

    public CropWeatherProfile getProfile(String cropName) {
        if (cropName == null) return null;
        String key = cropName.trim().toLowerCase(Locale.ROOT);
        if ("paddy".equals(key)) key = "rice";
        return profiles.get(key);
    }

    private StageConfig stage(String name, double startGdd, double maxT, double minT,
                              double rain, double minHumidity, double maxHumidity) {
        return new StageConfig(name, startGdd, maxT, minT, rain, minHumidity, maxHumidity);
    }

    private CropWeatherProfile groundnut() {
        return new CropWeatherProfile(
                "Groundnut", 13.3,
                "Base temperature 13.3°C (56°F), consistent with peanut GDD extension references. Stage thresholds are prototype values.",
                List.of(
                        stage("Germination", 0, 38, 18, 30, 40, 85),
                        stage("Seedling", 110, 38, 18, 30, 40, 85),
                        stage("Vegetative", 320, 40, 18, 35, 35, 90),
                        stage("Flowering / Pegging", 650, 35, 20, 25, 45, 85),
                        stage("Pod Development", 950, 36, 20, 25, 40, 85),
                        stage("Maturity", 1250, 38, 18, 20, 35, 80)
                ));
    }

    private CropWeatherProfile rice() {
        return new CropWeatherProfile(
                "Rice", 10.0,
                "Base temperature 10°C (50°F), consistent with common rice GDD references. Stage thresholds are prototype values.",
                List.of(
                        stage("Germination", 0, 38, 18, 40, 50, 95),
                        stage("Seedling", 100, 36, 18, 40, 50, 95),
                        stage("Vegetative / Tillering", 350, 38, 18, 50, 50, 95),
                        stage("Reproductive", 700, 35, 20, 40, 55, 95),
                        stage("Grain Filling", 1000, 35, 18, 30, 50, 90),
                        stage("Maturity", 1300, 36, 18, 25, 45, 90)
                ));
    }

    private CropWeatherProfile cotton() {
        return new CropWeatherProfile(
                "Cotton", 15.6,
                "Base temperature 15.6°C (60°F), the standard DD60 cotton heat-unit base. Stage thresholds are prototype values.",
                List.of(
                        stage("Germination", 0, 38, 16, 25, 35, 85),
                        stage("Seedling", 80, 38, 16, 25, 35, 85),
                        stage("Vegetative / Squaring", 250, 40, 18, 30, 30, 85),
                        stage("Flowering / Boll Set", 500, 38, 20, 25, 35, 85),
                        stage("Boll Development", 850, 38, 18, 20, 30, 80),
                        stage("Maturity", 1200, 38, 18, 15, 30, 80)
                ));
    }

    private CropWeatherProfile maize() {
        return new CropWeatherProfile(
                "Maize", 10.0,
                "Base temperature 10°C (50°F), a common corn/maize GDD base. Stage thresholds are prototype values.",
                List.of(
                        stage("Germination", 0, 38, 10, 30, 35, 90),
                        stage("Seedling", 100, 38, 12, 30, 35, 90),
                        stage("Vegetative", 300, 40, 15, 35, 35, 90),
                        stage("Tasseling / Silking", 650, 35, 18, 25, 40, 85),
                        stage("Grain Filling", 1000, 36, 16, 25, 35, 85),
                        stage("Maturity", 1300, 38, 15, 20, 30, 80)
                ));
    }

    private CropWeatherProfile tomato() {
        return new CropWeatherProfile(
                "Tomato", 10.0,
                "Base temperature 10°C (50°F), used in common tomato GDD reference tables. Stage thresholds are prototype values.",
                List.of(
                        stage("Establishment", 0, 35, 12, 25, 40, 90),
                        stage("Seedling", 100, 35, 12, 25, 40, 90),
                        stage("Vegetative", 300, 36, 14, 30, 40, 90),
                        stage("Flowering / Fruit Set", 600, 34, 16, 25, 45, 85),
                        stage("Fruit Development", 900, 35, 16, 25, 40, 85),
                        stage("Harvest / Maturity", 1150, 36, 14, 20, 35, 80)
                ));
    }

    private CropWeatherProfile chilli() {
        return new CropWeatherProfile(
                "Chilli", 10.0,
                "Base temperature 10°C (50°F), consistent with common pepper GDD reference tables. Stage thresholds are prototype values.",
                List.of(
                        stage("Establishment", 0, 35, 14, 25, 40, 90),
                        stage("Seedling", 100, 35, 14, 25, 40, 90),
                        stage("Vegetative", 300, 36, 16, 30, 40, 90),
                        stage("Flowering / Fruit Set", 600, 34, 18, 25, 45, 85),
                        stage("Fruit Development", 900, 35, 18, 25, 40, 85),
                        stage("Harvest / Maturity", 1200, 36, 16, 20, 35, 80)
                ));
    }
}
