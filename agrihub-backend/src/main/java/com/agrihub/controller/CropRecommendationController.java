package com.agrihub.controller;

import com.agrihub.model.CropRecommendationRequest;
import com.agrihub.model.CropRecommendationResponse;
import com.agrihub.model.CropSuitabilityResult;
import com.agrihub.service.CropRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@CrossOrigin(origins = "*")
public class CropRecommendationController {

    @Autowired
    private CropRecommendationService cropRecommendationService;

    /**
     * POST /recommendations/crop
     * Body: CropRecommendationRequest
     * Returns the top 3 crops with a rule-based Suitability Score (NOT ML accuracy).
     */
    @PostMapping("/crop")
    public ResponseEntity<CropRecommendationResponse> recommendCrop(
            @RequestBody CropRecommendationRequest request) {
        List<CropSuitabilityResult> results = cropRecommendationService.recommend(request);
        return ResponseEntity.ok(new CropRecommendationResponse(results));
    }
}
