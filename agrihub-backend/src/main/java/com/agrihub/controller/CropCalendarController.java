package com.agrihub.controller;

import com.agrihub.model.CropCalendarRequest;
import com.agrihub.model.CropCalendarResponse;
import com.agrihub.service.CropCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * NOTE on the endpoint path: the rest of AgriHub's backend (see
 * CropRecommendationController, WeatherController, ProductController, etc.)
 * uses no "/api" prefix anywhere. For consistency with that existing
 * convention, this endpoint is "/crop-calendar/generate" rather than
 * "/api/crop-calendar/generate".
 */
@RestController
@RequestMapping("/crop-calendar")
@CrossOrigin(origins = "*")
public class CropCalendarController {

    @Autowired
    private CropCalendarService cropCalendarService;

    @PostMapping("/generate")
    public ResponseEntity<CropCalendarResponse> generate(@RequestBody CropCalendarRequest request) {
        return ResponseEntity.ok(cropCalendarService.generate(request));
    }
}
