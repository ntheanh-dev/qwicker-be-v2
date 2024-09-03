package com.nta.repository;

import com.nta.entity.ShipperPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperPostRepository extends JpaRepository<ShipperPost, String> {

    @Query("SELECT p FROM ShipperPost p WHERE p.shipper.id = :shipperId AND p.post.id = :postId")
    Optional<ShipperPost> findByShipperIdAndPostId(@Param("shipperId") String shipperId,@Param("postId") String postId);

    List<ShipperPost> findAllByPostId(String postId);
}
