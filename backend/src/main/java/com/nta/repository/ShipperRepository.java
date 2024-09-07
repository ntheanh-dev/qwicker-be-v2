package com.nta.repository;

import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.enums.ShipperPostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, String> {
  Optional<Shipper> findByUserId(final String userId);

  Shipper findByUser(final User user);

  @Query("SELECT s.id FROM Shipper s WHERE s.user.id = :userId")
  String findShipperIdByUserId(@Param("userId") final String userId);

  @Query("SELECT p.shipper FROM ShipperPost p WHERE p.post.id = :postId AND p.status = :status")
  Shipper getWinShipperByPostId(@Param("postId") final String postId, @Param("status") final ShipperPostStatus status);
}
