package com.nta.controller;

import com.nta.dto.request.UserCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.UserResponse;
import com.nta.entity.User;
import com.nta.mapper.UserMapper;
import com.nta.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    UserService userService;
    UserMapper userMapper;
    @PostMapping(consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    ApiResponse<UserResponse> creationUser(@ModelAttribute @Valid UserCreationRequest request) {
        User u = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .result(userMapper.toUserResponse(u))
                .build();
    }
}
