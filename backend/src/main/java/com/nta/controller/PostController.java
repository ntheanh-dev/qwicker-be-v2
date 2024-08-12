package com.nta.controller;

import com.nta.dto.request.post.PostCreationRequest;
import com.nta.dto.response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {

    @PostMapping
    ApiResponse<Void> createPost(@RequestBody PostCreationRequest request) {
        return ApiResponse.<Void>builder().build();
    }
}
