package com.nta.mapper;

import com.nta.dto.request.UserCreationRequest;
import com.nta.dto.response.UserResponse;
import com.nta.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
