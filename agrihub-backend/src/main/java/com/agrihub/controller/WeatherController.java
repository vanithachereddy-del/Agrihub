package com.agrihub.controller;

import com.agrihub.model.WeatherEstimateResponse;
import com.agrihub.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@CrossOrigin(origins = "*")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    /**
     * GET /weather/estimate?location=Mylavaram
     * Resolves the location via Open-Meteo Geocoding, then classifies
     * recent + forecast rainfall into an Estimated Water Availability
     * (Low/Medium/High). See WeatherEstimateResponse for disclaimers.
     * Returns 404 if the location cannot be geocoded.
     */
    @GetMapping("/estimate")
    public ResponseEntity<WeatherEstimateResponse> estimate(@RequestParam String location) {
        return ResponseEntity.ok(weatherService.estimateWaterAvailability(location));
    }
}
