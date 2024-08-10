package com.nta.repository;

import com.nta.entity.Shipper;
import com.nta.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShipperRepositoy extends JpaRepository<Shipper,String> {
    Shipper findByUser(User user);
}
