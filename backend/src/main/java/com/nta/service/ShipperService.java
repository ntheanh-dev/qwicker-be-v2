package com.nta.service;

import com.nta.dto.request.ShipperCreationRequest;
import com.nta.entity.Role;
import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.entity.Vehicle;
import com.nta.exception.AppException;
import com.nta.enums.ErrorCode;
import com.nta.mapper.ShipperMapper;
import com.nta.mapper.UserMapper;
import com.nta.repository.ShipperRepositoy;
import com.nta.repository.VehicleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@RequiredArgsConstructor
public class ShipperService {

    ShipperRepositoy shipperRepositoy;
    ShipperMapper shipperMapper;

    UserService userService;
    UserMapper userMapper;

    VehicleService vehicleService;
    CloudinaryService cloudinaryService;

    RoleService roleService;

    @Transactional
    public Shipper create(ShipperCreationRequest request) {

        var role = roleService.findByName("ROLE_USER").orElseThrow(() ->
                new AppException(ErrorCode.ROLE_USER_NOT_FOUND));

        Vehicle v = vehicleService.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        User u = userService.createUser(userMapper.toUserCreationRequest(request));
        userService.updateRole(u,role);
        Shipper shipper = shipperMapper.toShipper(request);
        shipper.setUser(u);
        shipper.setVehicle(v);

        //upload identity photo to cloudinary
        try{
            Map cloudinaryResponse = cloudinaryService.upload(request.getIdentityFFile());
            Map cloudinaryResponse2 = cloudinaryService.upload(request.getIdentityBFile());
            shipper.setIdentityF(cloudinaryResponse.get("secure_url").toString());
            shipper.setIdentityB(cloudinaryResponse2.get("secure_url").toString());
        } catch (RuntimeException e) {
            throw new AppException(ErrorCode.CREATE_SHIPPER_FAILED);
        }

        return shipperRepositoy.save(shipper);
    }
}
