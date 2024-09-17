package com.nta.repository;

import com.nta.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,String> {
    @Query("SELECT p FROM Payment p WHERE p.post.id = :postId")
    Optional<Payment> findByPostId(@Param("postId") String postId);
}
