package com.nta.service;

import com.nta.entity.Vehicle;
import com.nta.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    Optional<Vehicle> findById(String id) {
        return vehicleRepository.findById(id);
    }
}
