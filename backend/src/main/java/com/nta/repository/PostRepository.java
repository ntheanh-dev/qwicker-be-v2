package com.nta.repository;

import com.nta.entity.Post;
import com.nta.enums.PostStatus;
import com.nta.enums.ShipperPostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
  @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
  List<Post> findPostsByUserId(@Param("userId") String userId);

  @Query(
      "SELECT p "
          + "FROM Post p "
          + "JOIN PostHistory ph ON p.id = ph.post.id "
          + "JOIN ( "
          + "    SELECT ph.post.id AS postId, MAX(ph.statusChangeDate) AS latestDate "
          + "    FROM PostHistory ph "
          + "    GROUP BY ph.post.id "
          + ") latestStatus ON ph.post.id = latestStatus.postId "
          + "AND ph.statusChangeDate = latestStatus.latestDate "
          + "AND ph.status = :status "
          + "AND p.user.id = :userId")
  List<Post> findPostsByLatestStatus(
      @Param("userId") String userId, @Param("status") PostStatus status);

  Optional<Post> findPostByIdAndStatus(String postId, PostStatus status);

  @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.status IN :statusList")
  List<Post> findPostsByStatus(
      @Param("userId") String userId, @Param("statusList") List<PostStatus> statusList);

  @Query(
      "SELECT p FROM Post p JOIN ShipperPost sp ON p.id = sp.post.id WHERE sp.shipper.id = :shipperId AND sp.status = :status  AND p.status IN :statusList")
  List<Post> findPostsByStatusAndShipperId(
      @Param("shipperId") String shipperId,
      @Param("status") ShipperPostStatus status,
      @Param("statusList") List<PostStatus> statusList);
}
