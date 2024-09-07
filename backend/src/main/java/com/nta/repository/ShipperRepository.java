package com.nta.repository;

import com.nta.entity.Shipper;
import com.nta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper,String> {
    Optional<Shipper> findByUserId(String userId);
    Shipper findByUser(User user);

    @Query("SELECT s.id FROM Shipper s WHERE s.user.id = :userId")
    String findShipperIdByUserId(@Param("userId") String userId);
}
