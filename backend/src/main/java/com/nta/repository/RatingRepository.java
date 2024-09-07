package com.nta.repository;

import com.nta.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, String> {

  @Query("SELECT r FROM Rating r WHERE r.post.id = :postId AND r.user.id = :userId")
  Optional<Rating> findByPostIdAndShipperId(
      @Param("postId") final String postId, @Param("userId") final String userId);
}
