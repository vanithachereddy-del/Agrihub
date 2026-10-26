package com.agrihub.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SINGLE CENTRALIZED SOURCE of crop activity schedules for the Crop
 * Calendar feature. No agricultural values are scattered elsewhere in the
 * code - everything crop-schedule-related lives here.
 * <p>
 * IMPORTANT: Crop activity intervals below are PROTOTYPE ADVISORY SCHEDULES
 * for demonstration purposes. They are general, commonly-known agriculture
 * patterns, not sourced from a specific validated dataset per crop
 * variety/region. They MUST be validated/customized for crop variety, soil,
 * region and local agricultural recommendations before real-world use.
 * <p>
 * This is intentionally kept independent of CropGuide/DataSeeder (the Crop
 * Recommendation module's data) so that this new feature cannot destabilize
 * the already-working recommendation module, and so Groundnut - which this
 * calendar supports but which is not yet in Crop Recommendation - can be
 * added without touching that module.
 */
@Component
public class CropCalendarTemplateProvider {

    /** One activity in a crop's schedule. */
    public static class ActivityTemplate {
        public final String activityName;
        public final String activityType;
        public final int dayAfterSowingMin;
        public final int dayAfterSowingMax;
        public final boolean weatherSensitive;
        public final String guidance;

        public ActivityTemplate(String activityName, String activityType, int dayAfterSowingMin,
                                 int dayAfterSowingMax, boolean weatherSensitive, String guidance) {
            this.activityName = activityName;
            this.activityType = activityType;
            this.dayAfterSowingMin = dayAfterSowingMin;
            this.dayAfterSowingMax = dayAfterSowingMax;
            this.weatherSensitive = weatherSensitive;
            this.guidance = guidance;
        }
    }

    /** A crop's full schedule template. */
    public static class CropTemplate {
        public final String cropName;
        public final int durationMinDays;
        public final int durationMaxDays;
        public final List<ActivityTemplate> activities;

        public CropTemplate(String cropName, int durationMinDays, int durationMaxDays,
                             List<ActivityTemplate> activities) {
            this.cropName = cropName;
            this.durationMinDays = durationMinDays;
            this.durationMaxDays = durationMaxDays;
            this.activities = activities;
        }
    }

    private final Map<String, CropTemplate> templates = new LinkedHashMap<>();

    public CropCalendarTemplateProvider() {
        register(groundnut());
        register(rice());
        register(cotton());
        register(maize());
        register(tomato());
        register(chilli());
    }

    private void register(CropTemplate template) {
        templates.put(template.cropName.toLowerCase(Locale.ROOT), template);
    }

    /** Case-insensitive lookup. "Paddy" is accepted as an alias for "Rice". */
    public CropTemplate getTemplate(String cropName) {
        if (cropName == null) {
            return null;
        }
        String key = cropName.trim().toLowerCase(Locale.ROOT);
        if (key.equals("paddy")) {
            key = "rice";
        }
        return templates.get(key);
    }

    public List<String> getSupportedCropNames() {
        List<String> names = new ArrayList<>();
        for (CropTemplate t : templates.values()) {
            names.add(t.cropName);
        }
        return names;
    }

    private CropTemplate groundnut() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Sowing", "SOWING", 0, 0, false,
                "Sow seeds at recommended spacing and depth for your variety."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 10, 10, false,
                "Check germination and seedling stand."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 15, 15, true,
                "Check soil moisture; irrigate if needed."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 20, 25, false,
                "Remove weeds competing with young plants."));
        activities.add(new ActivityTemplate("Fertilizer / Nutrient Activity", "FERTILIZER", 25, 30, true,
                "Apply as per soil test / local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 40, 40, false,
                "Scout for leaf miner, aphids and other common pests."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 45, 45, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Groundnut", 100, 120, activities);
    }

    private CropTemplate rice() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Sowing / Transplanting", "SOWING", 0, 0, false,
                "Transplant seedlings or direct-sow as per practice."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 10, 10, false,
                "Check establishment and standing water level."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 20, 20, true,
                "Rice needs consistent water - check field water level."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 25, 30, false,
                "Manage weeds; consider labour/herbicide as per practice."));
        activities.add(new ActivityTemplate("Fertilizer Application", "FERTILIZER", 30, 35, true,
                "Basal/top-dress nitrogen as per local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 45, 45, false,
                "Watch for stem borer and brown plant hopper."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 50, 50, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Rice", 120, 150, activities);
    }

    private CropTemplate cotton() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Sowing", "SOWING", 0, 0, false,
                "Sow seeds at recommended spacing."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 15, 15, false,
                "Check germination and seedling stand."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 25, 25, true,
                "Check soil moisture, especially near squaring stage."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 30, 35, false,
                "Manage weeds during early vegetative growth."));
        activities.add(new ActivityTemplate("Fertilizer Application", "FERTILIZER", 35, 40, true,
                "Apply as per soil test / local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 55, 55, false,
                "Watch for pink bollworm and whitefly."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 60, 60, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Cotton", 160, 180, activities);
    }

    private CropTemplate maize() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Sowing", "SOWING", 0, 0, false,
                "Sow seeds at recommended spacing and depth."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 10, 10, false,
                "Check germination and seedling stand."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 18, 18, true,
                "Check soil moisture, especially near tasseling."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 20, 25, false,
                "Manage weeds during early growth stages."));
        activities.add(new ActivityTemplate("Fertilizer Application", "FERTILIZER", 25, 30, true,
                "Apply as per soil test / local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 35, 35, false,
                "Watch for stem borer and fall armyworm."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 40, 40, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Maize", 90, 120, activities);
    }

    private CropTemplate tomato() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Sowing / Transplanting", "SOWING", 0, 0, false,
                "Transplant healthy seedlings."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 10, 10, false,
                "Check establishment and seedling health."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 15, 15, true,
                "Tomato needs consistent watering - check soil moisture."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 20, 20, false,
                "Remove weeds around plants."));
        activities.add(new ActivityTemplate("Fertilizer Application", "FERTILIZER", 25, 30, true,
                "Apply as per soil test / local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 35, 35, false,
                "Watch for fruit borer and whitefly."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 40, 40, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Tomato", 90, 120, activities);
    }

    private CropTemplate chilli() {
        List<ActivityTemplate> activities = new ArrayList<>();
        activities.add(new ActivityTemplate("Transplanting", "SOWING", 0, 0, false,
                "Transplant healthy seedlings."));
        activities.add(new ActivityTemplate("Field Inspection", "FIELD_INSPECTION", 10, 10, false,
                "Check establishment and seedling health."));
        activities.add(new ActivityTemplate("Irrigation Check", "IRRIGATION", 15, 15, true,
                "Check soil moisture; avoid waterlogging."));
        activities.add(new ActivityTemplate("Weed Management", "WEEDING", 20, 25, false,
                "Remove weeds around plants."));
        activities.add(new ActivityTemplate("Fertilizer Application", "FERTILIZER", 30, 30, true,
                "Apply as per soil test / local recommendation."));
        activities.add(new ActivityTemplate("Pest Monitoring", "PEST_MONITORING", 45, 45, false,
                "Watch for thrips and aphids."));
        activities.add(new ActivityTemplate("Spraying (if required)", "SPRAYING", 50, 50, true,
                "Spray only if pest/disease scouting indicates it is needed."));
        return new CropTemplate("Chilli", 150, 150, activities);
    }
}
