package com.nta.controller;

import com.nta.dto.request.CheckAccountRequest;
import com.nta.dto.request.UserCreationRequest;
import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.UserResponse;
import com.nta.entity.User;
import com.nta.mapper.UserMapper;
import com.nta.service.AuthenticationService;
import com.nta.service.UserService;
import com.nta.service.websocker.OnlineOfflineService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserController {
    UserService userService;
    UserMapper userMapper;
    OnlineOfflineService onlineOfflineService;
    @PostMapping
    ApiResponse<UserResponse> creationUser(@ModelAttribute UserCreationRequest request) {
        User newUser = userService.createUser(request);
        return ApiResponse.<UserResponse>builder()
                .result(userMapper.toUserResponse(newUser))
                .build();
    }

//    @PreAuthorize("hasAuthority('CREATE_POST')")
    @GetMapping
    ApiResponse<List<User>> getUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication(); // Lấy thông tin user đang được authen
        authentication.getAuthorities().forEach(s -> log.info("authority: {}", s.getAuthority()));
        return ApiResponse.<List<User>>builder()
                .result(userService.getAllUsers())
                .build();
    }

    @GetMapping("/my-info")
    @PreAuthorize("hasRole('USER')")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getMyInfo())
                .build();
    }

    @PostMapping("/check-account")
    ApiResponse<Void> verifyUsernameAndEmailNotExisted(@ModelAttribute CheckAccountRequest request) {
        userService.checkUsernameAndEmail(request);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/subscriptions")
    Map<String, Set<String>> getSubscriptions() {
        return onlineOfflineService.getUserSubscribed();
    }
}
