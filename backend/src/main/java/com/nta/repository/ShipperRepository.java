package com.nta.repository;

import com.nta.entity.Shipper;
import com.nta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper,String> {
    Optional<Shipper> findByUserId(String userId);
    Shipper findByUser(User user);
}
