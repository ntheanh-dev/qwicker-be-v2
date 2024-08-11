package com.nta.mapper;

import com.nta.dto.response.ProductCategoryResponse;
import com.nta.entity.ProductCategory;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductCategoryMapper {
    ProductCategoryResponse toResponse(ProductCategory prd);
}
