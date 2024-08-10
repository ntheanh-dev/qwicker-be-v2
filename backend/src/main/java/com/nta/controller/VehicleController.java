package com.nta.controller;

import com.nta.dto.response.ApiResponse;
import com.nta.entity.Vehicle;
import com.nta.service.VehicleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class VehicleController {
    VehicleService vehicleService;

    @GetMapping
    ApiResponse<List<Vehicle>> getUser() {

        return ApiResponse.<List<Vehicle>>builder()
                .result(vehicleService.findAll())
                .build();
    }
}
