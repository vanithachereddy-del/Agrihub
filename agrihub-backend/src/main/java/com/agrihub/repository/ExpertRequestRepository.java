package com.agrihub.repository;

import com.agrihub.model.ExpertRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpertRequestRepository extends MongoRepository<ExpertRequest, String> {
}
