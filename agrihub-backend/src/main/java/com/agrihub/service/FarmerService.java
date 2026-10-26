package com.agrihub.service;

import com.agrihub.model.Farmer;
import com.agrihub.repository.FarmerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FarmerService {

    @Autowired
    private FarmerRepository farmerRepository;

    public @NonNull Farmer registerFarmer(@NonNull Farmer farmer) {
        return farmerRepository.save(farmer);
    }

    public Optional<Farmer> login(String email, String password) {
        return farmerRepository.findByEmail(email)
                .filter(f -> f.getPassword().equals(password));
    }

    public List<Farmer> getAllFarmers() {
        return farmerRepository.findAll();
    }

    public Optional<Farmer> getFarmerById(@NonNull String id) {
        return farmerRepository.findById(id);
    }

    public Farmer updateFarmer(@NonNull String id, Farmer updatedFarmer) {
        return farmerRepository.findById(id).map(existing -> {
            existing.setFullName(updatedFarmer.getFullName());
            existing.setEmail(updatedFarmer.getEmail());
            existing.setPhone(updatedFarmer.getPhone());
            existing.setAddress(updatedFarmer.getAddress());
            existing.setVillage(updatedFarmer.getVillage());
            existing.setFarmName(updatedFarmer.getFarmName());
            existing.setFarmSize(updatedFarmer.getFarmSize());
            existing.setCrops(updatedFarmer.getCrops());
            return farmerRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Farmer not found"));
    }
}
