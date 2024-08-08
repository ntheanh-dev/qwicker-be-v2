package com.nta.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String username;
    String firstName;
    String lastName;
    String email;
    String avatar;
    Set<RoleResponse> roles;
}
