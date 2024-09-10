package com.nta.service;

import com.nta.constant.PredefinedRole;
import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.request.StatisticIncomeRequest;
import com.nta.dto.response.ShipperResponse;
import com.nta.dto.response.StatisticIncomeResponse;
import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.entity.Vehicle;
import com.nta.enums.PostStatus;
import com.nta.enums.ShipperPostStatus;
import com.nta.enums.StatisticIncomeType;
import com.nta.exception.AppException;
import com.nta.enums.ErrorCode;
import com.nta.mapper.ShipperMapper;
import com.nta.mapper.UserMapper;
import com.nta.repository.ShipperRepository;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ShipperService {

  ShipperRepository shipperRepository;
  ShipperMapper shipperMapper;

  UserRepository userRepository;
  UserService userService;
  UserMapper userMapper;

  VehicleService vehicleService;
  CloudinaryService cloudinaryService;

  @Transactional
  public Shipper create(final ShipperCreationRequest request) {
    final Vehicle v =
        vehicleService
            .findById(request.getVehicleId())
            .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

    User u = userService.createUser(userMapper.toUserCreationRequest(request));
    u = userService.addRole(u, PredefinedRole.SHIPPER_ROLE);
    Shipper shipper = shipperMapper.toShipper(request);
    shipper.setUser(u);
    shipper.setVehicle(v);

    // upload identity photo to cloudinary
    try {
      Map cloudinaryResponse = cloudinaryService.upload(request.getIdentityFFile());
      shipper.setIdentityF(cloudinaryResponse.get("secure_url").toString());
    } catch (RuntimeException e) {
      throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
    }

    return shipperRepository.save(shipper);
  }

  public ShipperResponse getMyInfo() {
    final Shipper s = getCurrentShipper();
    return shipperMapper.toShipperResponse(s);
  }

  public Shipper findById(final String id) {
    return shipperRepository
        .findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.SHIPPER_NOT_FOUND));
  }

  public Shipper getCurrentShipper() {
    final var context = SecurityContextHolder.getContext();
    final String name = context.getAuthentication().getName();
    final User user =
        userRepository
            .findByUsername(name)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    return shipperRepository.findByUser(user);
  }

  public String findShipperIdByUserId(String userId) {
    return shipperRepository.findShipperIdByUserId(userId);
  }

  public Optional<Vehicle> getVehicleByUserId(final String userId) {
    return shipperRepository.findByUserId(userId).map(Shipper::getVehicle);
  }

  public Shipper getWinShipperByPostId(final String postId) {
    return shipperRepository.getWinShipperByPostId(postId, ShipperPostStatus.APPROVAL);
  }

  @PreAuthorize("hasRole('SHIPPER')")
  public List<StatisticIncomeResponse> getStatistics(final StatisticIncomeRequest request) {
    StatisticIncomeType type = null;
    try {
      type = StatisticIncomeType.valueOf(request.getType());
    } catch (IllegalArgumentException e) {
      throw new AppException(ErrorCode.INVALID_STATISTIC_INCOME_TYPE);
    }

    final var shipper = getCurrentShipper();
    final List<StatisticIncomeResponse> response = new ArrayList<>();

    List<Object[]> results = null;
    if (type.equals(StatisticIncomeType.HOURLY)) {
      results =
          shipperRepository.findHourlyIncome(
              shipper.getId(), request.getStartDate(), request.getEndDate());
    }
    assert results != null;
    for (Object[] result : results) {
      response.add(toStatisticIncomeResponse(result, type));
    }

    return response;
  }

  private StatisticIncomeResponse toStatisticIncomeResponse(
      final Object[] result, final StatisticIncomeType type) {

    return StatisticIncomeResponse.builder()
        .type(type)
        .dateTime(
            LocalDateTime.parse(
                result[0].toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00")))
        .totalPayments((Long) result[1])
        .totalRevenue((BigDecimal) result[2])
        .cashRevenue((BigDecimal) result[3])
        .vnPayRevenue((BigDecimal) result[4])
        .build();
  }
}
