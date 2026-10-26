package com.agrihub.service;

import com.agrihub.model.CropGuide;
import com.agrihub.repository.CropGuideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CropGuideService {

    @Autowired
    private CropGuideRepository cropGuideRepository;

    public List<CropGuide> getAllCrops() {
        return cropGuideRepository.findAll();
    }

    public Optional<CropGuide> getCropById(@NonNull String id) {
        return cropGuideRepository.findById(id);
    }

    public @NonNull CropGuide addCrop(@NonNull CropGuide cropGuide) {
        return cropGuideRepository.save(cropGuide);
    }
}
