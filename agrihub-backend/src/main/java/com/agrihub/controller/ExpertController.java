package com.agrihub.controller;

import com.agrihub.model.ExpertRequest;
import com.agrihub.service.ExpertRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Expert consultation REST API.
 * Endpoints: GET/POST /expert, PUT /expert/{id}
 */
@RestController
@RequestMapping("/expert")
@CrossOrigin(origins = "*")
public class ExpertController {

    @Autowired
    private ExpertRequestService expertRequestService;

    @GetMapping
    public List<ExpertRequest> getAllRequests() {
        return expertRequestService.getAllRequests();
    }

    @PostMapping
    public ResponseEntity<ExpertRequest> createRequest(@RequestBody @NonNull ExpertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expertRequestService.createRequest(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRequest(@PathVariable @NonNull String id, @RequestBody ExpertRequest request) {
        return ResponseEntity.ok(expertRequestService.updateRequest(id, request));
    }

    /** Convenience endpoint for admin replies via JSON body { "reply": "..." } */
    @PutMapping("/{id}/reply")
    public ResponseEntity<?> replyToRequest(@PathVariable @NonNull String id,
            @RequestBody Map<String, String> payload) {
        String reply = payload.get("reply");
        return ResponseEntity.ok(expertRequestService.replyToRequest(id, reply));
    }
}
