package com.nta.service;

import com.nta.constant.PredefinedRole;
import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.entity.Vehicle;
import com.nta.exception.AppException;
import com.nta.enums.ErrorCode;
import com.nta.mapper.ShipperMapper;
import com.nta.mapper.UserMapper;
import com.nta.repository.ShipperRepository;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
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
    public Shipper create(ShipperCreationRequest request) {
        Vehicle v = vehicleService.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        User u = userService.createUser(userMapper.toUserCreationRequest(request));
        u = userService.addRole(u,PredefinedRole.SHIPPER_ROLE);
        Shipper shipper = shipperMapper.toShipper(request);
        shipper.setUser(u);
        shipper.setVehicle(v);

        //upload identity photo to cloudinary
        try{
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

    public Shipper getCurrentShipper() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();
        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return shipperRepository.findByUser(user);
    }

    public Optional<Vehicle> getVehicleByUserId(String userId) {
        return shipperRepository.findByUserId(userId)
                .map(Shipper::getVehicle);
    }
}
