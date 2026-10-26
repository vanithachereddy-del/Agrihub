package com.agrihub.service;

import com.agrihub.model.CropGuide;
import com.agrihub.model.CropRecommendationRequest;
import com.agrihub.model.CropSuitabilityResult;
import com.agrihub.repository.CropGuideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Rule-based Crop Recommendation engine.
 * <p>
 * This is intentionally NOT a machine-learning model. It scores each crop in
 * the "crops" collection against the farmer's inputs using fixed weights:
 * <ul>
 *   <li>Soil type match          - 25 points</li>
 *   <li>Season match             - 25 points</li>
 *   <li>Water availability match - 20 points</li>
 *   <li>Irrigation compatibility - 15 points (crop-specific, see below)</li>
 *   <li>pH suitability           - 15 points (only if the farmer supplied pH)</li>
 * </ul>
 * Any factor the farmer did not provide, or the crop has no reference data
 * for, is removed from both the numerator and denominator, so the score
 * always lands between 0 and 100 regardless of which optional fields were
 * filled in.
 * <p>
 * IRRIGATION SCORING: earlier versions treated "Rainfed" as universally
 * worse than assured irrigation. That is not accurate - some crops (e.g.
 * Cotton in Deccan black-soil tracts, Kharif Maize, lowland Kharif Rice in
 * high-rainfall belts) are routinely and successfully grown rainfed, while
 * others (Tomato, Rabi Wheat) need more assured watering. So each crop now
 * carries its own list of commonly compatible irrigation sources
 * (CropGuide#irrigationSources), and the farmer's chosen source is scored
 * against THAT list rather than a universal ranking. These per-crop lists
 * are general reference judgments, not a validated irrigation trial
 * dataset, and should be checked against local agricultural extension
 * guidance.
 * <p>
 * Previous crop and NPK values are still recorded and surfaced as
 * informational notes only - they are not scored yet, pending validated
 * rotation/nutrient threshold data for a future enhancement.
 */
@Service
public class CropRecommendationService {

    private static final double SOIL_WEIGHT = 25;
    private static final double SEASON_WEIGHT = 25;
    private static final double WATER_WEIGHT = 20;
    private static final double IRRIGATION_WEIGHT = 15;
    private static final double PH_WEIGHT = 15;

    @Autowired
    private CropGuideRepository cropGuideRepository;

    public List<CropSuitabilityResult> recommend(CropRecommendationRequest request) {
        List<CropGuide> crops = cropGuideRepository.findAll();
        List<CropSuitabilityResult> results = new ArrayList<>();

        for (CropGuide crop : crops) {
            CropSuitabilityResult result = score(crop, request);
            if (result != null) {
                results.add(result);
            }
        }

        results.sort(Comparator.comparingDouble(CropSuitabilityResult::getSuitabilityScore).reversed());

        return results.size() > 3 ? new ArrayList<>(results.subList(0, 3)) : results;
    }

    private CropSuitabilityResult score(CropGuide crop, CropRecommendationRequest request) {
        if (isBlank(crop.getWaterRequirement()) && isEmpty(crop.getSoilTypes()) && isEmpty(crop.getSeasons())) {
            return null;
        }

        double maxPossible = 0;
        double earned = 0;
        List<String> matchedFactors = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        // --- Soil type ---
        maxPossible += SOIL_WEIGHT;
        if (!isBlank(request.getSoilType()) && !isEmpty(crop.getSoilTypes())) {
            boolean soilMatch = crop.getSoilTypes().stream()
                    .anyMatch(s -> containsIgnoreCase(s, request.getSoilType())
                            || containsIgnoreCase(request.getSoilType(), s));
            if (soilMatch) {
                earned += SOIL_WEIGHT;
                matchedFactors.add("Soil type matches (" + request.getSoilType() + ")");
            } else {
                notes.add("Soil type '" + request.getSoilType() + "' does not match this crop's usual soil ("
                        + String.join(", ", crop.getSoilTypes()) + ")");
            }
        }

        // --- Season ---
        maxPossible += SEASON_WEIGHT;
        if (!isBlank(request.getSeason()) && !isEmpty(crop.getSeasons())) {
            boolean seasonMatch = crop.getSeasons().stream()
                    .anyMatch(s -> s.equalsIgnoreCase(request.getSeason()));
            if (seasonMatch) {
                earned += SEASON_WEIGHT;
                matchedFactors.add("Season matches (" + request.getSeason() + ")");
            } else {
                notes.add("Season '" + request.getSeason() + "' is not the usual season for this crop ("
                        + String.join(", ", crop.getSeasons()) + ")");
            }
        }

        // --- Water availability ---
        maxPossible += WATER_WEIGHT;
        if (!isBlank(request.getWaterAvailability()) && !isBlank(crop.getWaterRequirement())) {
            String farmerLevel = request.getWaterAvailability().trim().toLowerCase(Locale.ROOT);
            String cropLevel = crop.getWaterRequirement().trim().toLowerCase(Locale.ROOT);
            int diff = Math.abs(waterRank(farmerLevel) - waterRank(cropLevel));
            if (diff == 0) {
                earned += WATER_WEIGHT;
                matchedFactors.add("Water availability matches crop's " + crop.getWaterRequirement() + " requirement");
            } else if (diff == 1) {
                earned += WATER_WEIGHT * 0.5;
                notes.add("Water availability is close to, but not exactly, this crop's "
                        + crop.getWaterRequirement() + " requirement");
            } else {
                notes.add("Water availability does not match this crop's "
                        + crop.getWaterRequirement() + " requirement");
            }
        }

        // --- Irrigation compatibility (crop-specific, see class-level note) ---
        if (!isBlank(request.getIrrigationSource()) && !isEmpty(crop.getIrrigationSources())) {
            maxPossible += IRRIGATION_WEIGHT;
            boolean irrigationMatch = crop.getIrrigationSources().stream()
                    .anyMatch(s -> s.equalsIgnoreCase(request.getIrrigationSource()));
            if (irrigationMatch) {
                earned += IRRIGATION_WEIGHT;
                matchedFactors.add("Irrigation source (" + request.getIrrigationSource()
                        + ") is commonly used for this crop");
            } else {
                notes.add("Irrigation source '" + request.getIrrigationSource()
                        + "' is not typically used for this crop (commonly used: "
                        + String.join(", ", crop.getIrrigationSources()) + ")");
            }
            notes.add("Irrigation compatibility for " + crop.getName()
                    + " is a general reference assumption, not a validated irrigation trial result - "
                    + "check against local agricultural extension guidance.");
        }

        // --- pH (optional) ---
        if (request.getPh() != null && crop.getPhMin() != null && crop.getPhMax() != null) {
            maxPossible += PH_WEIGHT;
            if (request.getPh() >= crop.getPhMin() && request.getPh() <= crop.getPhMax()) {
                earned += PH_WEIGHT;
                matchedFactors.add("Soil pH " + request.getPh() + " is within the suitable range ("
                        + crop.getPhMin() + " to " + crop.getPhMax() + ")");
            } else {
                notes.add("Soil pH " + request.getPh() + " is outside the suitable range ("
                        + crop.getPhMin() + " to " + crop.getPhMax() + ")");
            }
        }

        // --- Informational-only factors (not scored yet) ---
        if (!isBlank(request.getPreviousCrop()) && request.getPreviousCrop().equalsIgnoreCase(crop.getName())) {
            notes.add("Same as previous crop - growing it again may not be ideal for crop rotation or soil health. "
                    + "This is not scored numerically as there is no validated rotation dataset yet.");
        }
        if (request.getNitrogen() != null || request.getPhosphorus() != null || request.getPotassium() != null) {
            notes.add("N/P/K values were recorded but are not yet used in scoring, pending validated "
                    + "crop-specific nutrient thresholds.");
        }
        if (!isBlank(request.getFarmLocation())) {
            notes.add("Farm location recorded for weather-based water availability estimation.");
        }

        double normalizedScore = maxPossible > 0 ? (earned / maxPossible) * 100.0 : 0.0;

        CropSuitabilityResult result = new CropSuitabilityResult();
        result.setCropId(crop.getId());
        result.setCropName(crop.getName());
        result.setCategory(crop.getCategory());
        result.setSuitabilityScore(Math.round(normalizedScore * 10.0) / 10.0);
        result.setMatchedFactors(matchedFactors);
        result.setNotes(notes);
        result.setWaterRequirement(crop.getWaterRequirement());
        result.setExpectedDuration(crop.getHarvestDuration());
        result.setReason(buildReason(matchedFactors));
        return result;
    }

    private String buildReason(List<String> matchedFactors) {
        if (matchedFactors.isEmpty()) {
            return "No strong matches found for the given inputs.";
        }
        return "Recommended mainly because: " + String.join("; ", matchedFactors) + ".";
    }

    private int waterRank(String level) {
        switch (level) {
            case "low":
                return 0;
            case "medium":
                return 1;
            case "high":
                return 2;
            default:
                return 1;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isEmpty(List<String> list) {
        return list == null || list.isEmpty();
    }

    private boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}
