package com.nta.service;

import com.nta.dto.response.ProductCategoryResponse;
import com.nta.entity.ProductCategory;
import com.nta.mapper.ProductCategoryMapper;
import com.nta.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryService {
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    public List<ProductCategoryResponse> findAll() {
        List<ProductCategory> result = productCategoryRepository.findAll();
        return result.stream().map(productCategoryMapper::toResponse).toList();
    }
}
