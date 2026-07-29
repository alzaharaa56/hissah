package com.hissah.Services;


import com.hissah.DTO.Request.CategoryRequestDTO;
import com.hissah.DTO.Response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO create(CategoryRequestDTO request);

    CategoryResponseDTO update(
            Long categoryId,
            CategoryRequestDTO request
    );

    CategoryResponseDTO deactivate(Long categoryId);

    CategoryResponseDTO getById(Long categoryId);

    List<CategoryResponseDTO> getActiveCategories();

    List<CategoryResponseDTO> getAllCategories();
}