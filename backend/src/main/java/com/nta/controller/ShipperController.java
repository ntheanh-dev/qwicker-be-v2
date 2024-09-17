package com.nta.controller;

import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Post;
import com.nta.mapper.ShipperMapper;
import com.nta.model.ShipperDetailCache;
import com.nta.service.PostService;
import com.nta.service.ShipperService;
import com.nta.service.websocker.LocationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shippers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ShipperController {
    ShipperService shipperService;
    ShipperMapper shipperMapper;
    LocationService locationService;
    PostService postService;
    @PostMapping
    ApiResponse<ShipperResponse> create(@ModelAttribute @Valid ShipperCreationRequest request) {
        ShipperResponse response = shipperMapper.toShipperResponse(shipperService.create(request));
        return ApiResponse.<ShipperResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/my-post")
    @PreAuthorize("hasRole('SHIPPER')")
    ApiResponse<List<Post>> getAllPosts(
            @RequestParam(value = "status", required = false) String statusList) {
        List<Post> response;
        try {
            response = postService.getShipperPostsByStatusList(statusList);
        } catch (IllegalArgumentException e) {
            response = List.of();
        }

        return ApiResponse.<List<Post>>builder().result(response).build();
    }

    @GetMapping("/my-info")
    @PreAuthorize("hasRole('SHIPPER')")
    ApiResponse<ShipperResponse> getMyInfo() {
        return ApiResponse.<ShipperResponse>builder()
                .result(shipperService.getMyInfo())
                .build();
    }

    @GetMapping("/{id}/current-location")
    ApiResponse<ShipperDetailCache> getCurrentLocation(@PathVariable String id) {
        return ApiResponse.<ShipperDetailCache>builder()
                .result(locationService.getCurrentShipperLocation(id))
                .build();
    }
}
