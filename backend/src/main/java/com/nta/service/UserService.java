package com.nta.service;

import com.nta.dto.request.UserCreationRequest;
import com.nta.entity.User;
import com.nta.exception.AppException;
import com.nta.exception.ErrorCode;
import com.nta.mapper.UserMapper;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class UserService {
    Logger log = LoggerFactory.getLogger(UserService.class);
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    CloudinaryService cloudinaryService;
    private static final String DEFAULT_AVATAR_URL = "https://res.cloudinary.com/dqpo9h5s2/image/upload/v1711860995/rooms/avatar_vuwmxd.jpg";
    public User createUser(UserCreationRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        User u = userMapper.toUser(request);
        u.setPassword(passwordEncoder.encode(request.getPassword()));

        //upload avatar to cloudinary
        try{
            Map cloudinaryResponse = cloudinaryService.upload(request.getFile());
            u.setAvatar(cloudinaryResponse.get("secure_url").toString());
        } catch (RuntimeException e) {
            u.setAvatar(DEFAULT_AVATAR_URL);
            log.warn("Cannot upload avatar to cloudinary, use default img url instead");
        }

        return userRepository.save(u);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
