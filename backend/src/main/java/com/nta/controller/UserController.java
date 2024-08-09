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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
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

//    @PreAuthorize("hasAuthority('CREATE_POST')")
    @GetMapping
    ApiResponse<List<User>> getUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy thông tin user đang được authen

        log.info("username: {}", authentication.getName());
        authentication.getAuthorities().forEach(s -> log.info("authority: {}", s.getAuthority()));

        return ApiResponse.<List<User>>builder()
                .result(userService.getAllUsers())
                .build();
    }
}
