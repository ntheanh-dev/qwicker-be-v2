package com.nta.repository;

import com.nta.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VehicleRepository extends JpaRepository<Vehicle,String> {
    Optional<Vehicle> findById(String id);
}
