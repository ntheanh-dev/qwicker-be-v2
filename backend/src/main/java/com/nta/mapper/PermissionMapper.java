package com.nta.mapper;
import com.nta.dto.request.PermissionRequest;
import com.nta.dto.response.PermissionResponse;
import com.nta.entity.Permission;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}