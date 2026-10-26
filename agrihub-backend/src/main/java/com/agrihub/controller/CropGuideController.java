package com.agrihub.controller;

import com.agrihub.model.CropGuide;
import com.agrihub.service.CropGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crops")
@CrossOrigin(origins = "*")
public class CropGuideController {

    @Autowired
    private CropGuideService cropGuideService;

    @GetMapping
    public List<CropGuide> getAllCrops() {
        return cropGuideService.getAllCrops();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCropById(@PathVariable @NonNull String id) {
        return cropGuideService.getCropById(id)
                .map(crop -> ResponseEntity.ok((Object) crop))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Crop not found"));
    }

    @PostMapping
    public ResponseEntity<CropGuide> addCrop(@RequestBody @NonNull CropGuide cropGuide) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cropGuideService.addCrop(cropGuide));
    }
}
