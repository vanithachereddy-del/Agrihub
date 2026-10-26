package com.agrihub.service;

import com.agrihub.model.ExpertRequest;
import com.agrihub.repository.ExpertRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpertRequestService {

    @Autowired
    private ExpertRequestRepository expertRequestRepository;

    public @NonNull ExpertRequest createRequest(@NonNull ExpertRequest request) {
        return expertRequestRepository.save(request);
    }

    public List<ExpertRequest> getAllRequests() {
        return expertRequestRepository.findAll();
    }

    public Optional<ExpertRequest> getRequestById(@NonNull String id) {
        return expertRequestRepository.findById(id);
    }

    public ExpertRequest replyToRequest(@NonNull String id, String reply) {
        return expertRequestRepository.findById(id).map(request -> {
            request.setAdminReply(reply);
            request.setStatus("Answered");
            return expertRequestRepository.save(request);
        }).orElseThrow(() -> new RuntimeException("Expert request not found"));
    }

    /** Full update for PUT /expert/{id} — used by admin to reply or change status. */
    public ExpertRequest updateRequest(@NonNull String id, ExpertRequest updated) {
        return expertRequestRepository.findById(id).map(request -> {
            if (updated.getAdminReply() != null) {
                request.setAdminReply(updated.getAdminReply());
                // Only set "Answered" as default if no explicit status is supplied
                if (updated.getStatus() == null) {
                    request.setStatus("Answered");
                }
            }
            if (updated.getStatus() != null) {
                request.setStatus(updated.getStatus());
            }
            return expertRequestRepository.save(request);
        }).orElseThrow(() -> new RuntimeException("Expert request not found"));
    }
}
