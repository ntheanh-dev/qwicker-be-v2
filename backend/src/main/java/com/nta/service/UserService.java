package com.nta.service;

import com.nta.constant.PredefinedRole;
import com.nta.dto.request.CheckAccountRequest;
import com.nta.dto.request.UserCreationRequest;
import com.nta.dto.response.UserResponse;
import com.nta.entity.Role;
import com.nta.entity.User;
import com.nta.exception.AppException;
import com.nta.enums.ErrorCode;
import com.nta.mapper.UserMapper;
import com.nta.repository.RoleRepository;
import com.nta.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
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
    RoleRepository roleRepository;
    private static final String DEFAULT_AVATAR_URL = "https://res.cloudinary.com/dqpo9h5s2/image/upload/v1711860995/rooms/avatar_vuwmxd.jpg";

    public User createUser(UserCreationRequest request) {
        User u = userMapper.toUser(request);
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        u.setRoles(roles);
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
    public User addRole(User u, String roleName) {
        roleRepository.findById(roleName).ifPresent(u.getRoles()::add);
        return userRepository.save(u);
    }
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public List<User> getAllUsers() {return userRepository.findAll();}
    public void checkUsernameAndEmail(CheckAccountRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
    }
    public UserResponse getMyInfo() {
        var user = currentUser();
        return userMapper.toUserResponse(user);
    }

    public User currentUser() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
