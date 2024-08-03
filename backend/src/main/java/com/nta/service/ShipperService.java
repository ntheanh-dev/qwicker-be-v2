package com.nta.service;

import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Shipper;
import com.nta.entity.User;
import com.nta.entity.Vehicle;
import com.nta.exception.AppException;
import com.nta.exception.ErrorCode;
import com.nta.mapper.ShipperMapper;
import com.nta.mapper.UserMapper;
import com.nta.repository.ShipperRepositoy;
import com.nta.repository.UserRepository;
import com.nta.repository.VehicleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    VehicleRepository vehicleRepository;

    CloudinaryService cloudinaryService;

    @Transactional
    public Shipper create(ShipperCreationRequest request) {

        Vehicle v = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new AppException(ErrorCode.VEHICLE_NOT_FOUND));

        User u = userService.createUser(userMapper.toUserCreationRequest(request));
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
