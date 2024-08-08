package com.nta.mapper;

import com.nta.dto.request.RoleRequest;
import com.nta.dto.response.RoleResponse;
import com.nta.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}