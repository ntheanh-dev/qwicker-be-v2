package com.nta.controller;

import com.nta.dto.response.ApiResponse;
import com.nta.dto.response.ProductCategoryResponse;
import com.nta.service.ProductCategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ProductController {
    ProductCategoryService productCategoryService;

    @GetMapping("/categories")
    ApiResponse<List<ProductCategoryResponse>> findAllProductCategory() {
        return ApiResponse.<List<ProductCategoryResponse>>builder()
                .result(productCategoryService.findAll())
                .build();
    }

}
