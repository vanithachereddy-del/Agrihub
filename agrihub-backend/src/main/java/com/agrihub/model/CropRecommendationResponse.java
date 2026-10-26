package com.agrihub.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level response for POST /recommendations/crop.
 * Always carries an explicit disclaimer so the frontend/report writer never
 * mistakes the suitability score for a validated ML accuracy figure.
 */
public class CropRecommendationResponse {

    private List<CropSuitabilityResult> recommendations = new ArrayList<>();
    private final String scoringMethod = "RULE_BASED";
    private final String disclaimer =
            "This is a rule-based Suitability Score derived from weighted matching of soil, "
            + "season, water availability and pH. It is NOT the accuracy of a trained machine "
            + "learning model. No labelled dataset has been used to evaluate this logic yet.";

    public CropRecommendationResponse() {
    }

    public CropRecommendationResponse(List<CropSuitabilityResult> recommendations) {
        this.recommendations = recommendations;
    }

    public List<CropSuitabilityResult> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<CropSuitabilityResult> recommendations) {
        this.recommendations = recommendations;
    }

    public String getScoringMethod() {
        return scoringMethod;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}
