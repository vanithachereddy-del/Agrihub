package com.agrihub.repository;

import com.agrihub.model.CropGuide;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CropGuideRepository extends MongoRepository<CropGuide, String> {
}
