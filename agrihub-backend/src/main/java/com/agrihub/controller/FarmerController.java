package com.agrihub.controller;

import com.agrihub.model.Farmer;
import com.agrihub.service.FarmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/farmers")
@CrossOrigin(origins = "*")
public class FarmerController {

    @Autowired
    private FarmerService farmerService;

    @PostMapping
    public ResponseEntity<?> register(@RequestBody @NonNull Farmer farmer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(farmerService.registerFarmer(farmer));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        return farmerService.login(email, password)
                .map(farmer -> ResponseEntity.ok((Object) farmer))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
    }

    @GetMapping
    public List<Farmer> getAllFarmers() {
        return farmerService.getAllFarmers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFarmerById(@PathVariable @NonNull String id) {
        return farmerService.getFarmerById(id)
                .map(farmer -> ResponseEntity.ok((Object) farmer))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Farmer not found"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFarmer(@PathVariable @NonNull String id, @RequestBody Farmer farmer) {
        return ResponseEntity.ok(farmerService.updateFarmer(id, farmer));
    }
}
