package com.nta.controller;

import com.nta.dto.request.RatingCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.service.RatingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatingController {

    RatingService ratingService;
    @PostMapping
    ApiResponse<Void> createPost(@RequestBody RatingCreationRequest request) {
        ratingService.createRating(request);
        return ApiResponse.<Void>builder()
                .build();
    }

}
