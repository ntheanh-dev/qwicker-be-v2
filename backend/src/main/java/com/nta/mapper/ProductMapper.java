package com.nta.mapper;

import com.nta.dto.request.post.ProductCreationRequest;
import com.nta.dto.response.ProductResponse;
import com.nta.entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toProductResponse(Product request);
    Product toProduct(ProductCreationRequest request);
}
