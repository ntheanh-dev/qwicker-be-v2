package com.nta.mapper;

import com.nta.dto.request.LocationCreationRequest;
import com.nta.entity.Location;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location toLocation(LocationCreationRequest request);
}
