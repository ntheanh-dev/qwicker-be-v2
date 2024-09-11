package com.nta.repository;

import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.enums.ShipperPostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipperRepository extends JpaRepository<Shipper, String> {
  Optional<Shipper> findByUserId(final String userId);

  Shipper findByUser(final User user);

  @Query("SELECT s.id FROM Shipper s WHERE s.user.id = :userId")
  String findShipperIdByUserId(@Param("userId") final String userId);

  @Query("SELECT p.shipper FROM ShipperPost p WHERE p.post.id = :postId AND p.status = :status")
  Shipper getWinShipperByPostId(
      @Param("postId") final String postId, @Param("status") final ShipperPostStatus status);

  @Query(
      "SELECT "
          + "DATE_FORMAT(pm.paidAt, '%Y-%m-%d %H:00:00') AS dateTime, "
          + "COUNT(pm.id) AS totalPayments, "
          + "SUM(pm.price) AS totalRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Tiền Mặt' THEN pm.price ELSE 0 END) AS cashRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Vn Pay' THEN pm.price ELSE 0 END) AS vnPayRevenue "
          + "FROM ShipperPost sp "
          + "JOIN sp.post p "
          + "JOIN Payment pm ON p.id = pm.post.id "
          + "WHERE p.status = 'DELIVERED' "
          + "AND pm.paidAt BETWEEN :startDate AND :endDate "
          + "AND sp.shipper.id = :shipperId "
          + "AND sp.status = 'APPROVAL' "
          + "GROUP BY DATE_FORMAT(pm.paidAt, '%Y-%m-%d %H:00:00') "
          + "ORDER BY DATE_FORMAT(pm.paidAt, '%Y-%m-%d %H:00:00')")
  List<Object[]> findHourlyIncome(
      @Param("shipperId") String shipperId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT "
          + "DATE(pm.paidAt) AS dateTime, "
          + "COUNT(pm.id) AS totalPayments, "
          + "SUM(pm.price) AS totalRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Tiền Mặt' THEN pm.price ELSE 0 END) AS cashRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Vn Pay' THEN pm.price ELSE 0 END) AS vnPayRevenue "
          + "FROM ShipperPost sp "
          + "JOIN sp.post p "
          + "JOIN Payment pm ON p.id = pm.post.id "
          + "WHERE p.status = 'DELIVERED' "
          + "AND pm.paidAt BETWEEN :startDate AND :endDate "
          + "AND sp.shipper.id = :shipperId "
          + "AND sp.status = 'APPROVAL' "
          + "GROUP BY DATE(pm.paidAt) "
          + "ORDER BY DATE(pm.paidAt)")
  public List<Object[]> findDailyIncome(
      @Param("shipperId") String shipperId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT "
          + "DATE_FORMAT(pm.paidAt, '%Y-%m') AS month, "
          + "COUNT(pm.id) AS totalPayments, "
          + "SUM(pm.price) AS totalRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Tiền Mặt' THEN pm.price ELSE 0 END) AS cashRevenue, "
          + "SUM(CASE WHEN pm.method.name = 'Vn Pay' THEN pm.price ELSE 0 END) AS vnPayRevenue "
          + "FROM ShipperPost sp "
          + "JOIN sp.post p "
          + "JOIN Payment pm ON p.id = pm.post.id "
          + "WHERE p.status = 'DELIVERED' "
          + "AND pm.paidAt BETWEEN :startDate AND :endDate "
          + "AND sp.shipper.id = :shipperId "
          + "AND sp.status = 'APPROVAL' "
          + "GROUP BY DATE_FORMAT(pm.paidAt, '%Y-%m') "
          + "ORDER BY DATE_FORMAT(pm.paidAt, '%Y-%m')")
  List<Object[]> findMonthlyIncome(
      @Param("shipperId") String shipperId,
      @Param("startDate") LocalDateTime startDate,
      @Param("endDate") LocalDateTime endDate);
}
