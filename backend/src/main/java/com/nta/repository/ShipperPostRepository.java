package com.nta.repository;

import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Shipper;
import com.nta.entity.ShipperPost;
import com.nta.enums.ShipperPostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperPostRepository extends JpaRepository<ShipperPost, String> {

  @Query("SELECT p FROM ShipperPost p WHERE p.shipper.id = :shipperId AND p.post.id = :postId")
  Optional<ShipperPost> findByShipperIdAndPostId(
      @Param("shipperId") final String shipperId, @Param("postId") final String postId);

  List<ShipperPost> findAllByPostId(final String postId);

  @Query("SELECT COUNT(p) > 0 FROM ShipperPost p WHERE p.post.id = :postId")
  boolean existsByPostId(@Param("postId") final String postId);

  @Query("SELECT COUNT(p) FROM ShipperPost p WHERE p.post.id = :postId")
  long countByPostId(@Param("postId") final String postId);

  @Query(
      "SELECT s.shipper FROM ShipperPost s "
          + "JOIN Post p ON s.post.id = p.id "
          + "WHERE s.status = :status AND p.user.id = :userId AND p.id = :postId")
  Shipper findWinnerByPostId(
      @Param("status") ShipperPostStatus status,
      @Param("userId") String userId,
      @Param("postId") String postId);

  @Query(
      "SELECT sp.shipper FROM ShipperPost sp WHERE sp.post.id = :postId AND sp.post.user.id = :userId AND sp.status = :status")
  List<Shipper> findApprovedShippersByPostAndUser(
      @Param("postId") String postId,
      @Param("userId") String userId,
      @Param("status") ShipperPostStatus status);

  ShipperPost findByPostIdAndShipperId(String postId, String shipperId);
}
