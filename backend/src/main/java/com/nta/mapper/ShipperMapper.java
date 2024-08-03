package com.nta.mapper;


import com.nta.dto.request.ShipperCreationRequest;
import com.nta.dto.response.ShipperResponse;
import com.nta.entity.Shipper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipperMapper {
    Shipper toShipper(ShipperCreationRequest request);
    ShipperResponse toShipperResponse(Shipper s);
}
