package com.nta.controller;

import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.ShipperResponse;
import com.nta.service.ShipperService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shippers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ShipperController {
    ShipperService shipperService;

    @PostMapping
    ApiResponse<ShipperResponse> create(@RequestBody @Valid ShipperCreationRequest request) {
        return ApiResponse.<ShipperResponse>builder()
                .result(shipperService.create(request))
                .build();
    }
}
