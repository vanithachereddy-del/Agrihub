package com.agrihub.controller;

import com.agrihub.model.DiseasePrediction;
import com.agrihub.service.DiseaseDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Placeholder AI disease detection endpoint.
 * POST /disease/detect accepts a crop image and returns a mock prediction.
 */
@RestController
@RequestMapping("/disease")
@CrossOrigin(origins = "*")
public class DiseaseController {

    @Autowired
    private DiseaseDetectionService diseaseDetectionService;

    @PostMapping("/detect")
    public ResponseEntity<DiseasePrediction> detect(@RequestParam("image") MultipartFile image) throws Exception {
        return ResponseEntity.ok(diseaseDetectionService.detect(image));
    }
}
