package com.nta.service;

import com.nta.dto.request.UserCreationRequest;
import com.nta.dto.response.UserResponse;
import com.nta.entity.User;
import com.nta.exception.AppException;
import com.nta.exception.ErrorCode;
import com.nta.mapper.UserMapper;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        User u = userMapper.toUser(request);
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        User savedUser = userRepository.save(u);
        return userMapper.toUserResponse(savedUser);
    }

}
