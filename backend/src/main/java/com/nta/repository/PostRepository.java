package com.nta.repository;

import com.nta.entity.Post;
import com.nta.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    List<Post> findPostsByUserId(@Param("userId") String userId);

    @Query("SELECT p " +
            "FROM Post p " +
            "JOIN PostHistory ph ON p.id = ph.post.id " +
            "JOIN ( " +
            "    SELECT ph.post.id AS postId, MAX(ph.statusChangeDate) AS latestDate " +
            "    FROM PostHistory ph " +
            "    GROUP BY ph.post.id " +
            ") latestStatus ON ph.post.id = latestStatus.postId " +
            "AND ph.statusChangeDate = latestStatus.latestDate " +
            "AND ph.status = :status " +
            "AND p.user.id = :userId"
    )
    List<Post> findPostsByLatestStatus(@Param("userId") String userId,@Param("status") PostStatus status);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.status IN :statusList")
    List<Post> findPostsByStatus(@Param("userId") String userId,@Param("statusList") List<PostStatus> statusList);
}
