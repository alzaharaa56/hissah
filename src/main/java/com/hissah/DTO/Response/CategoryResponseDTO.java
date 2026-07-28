package com.hissah.DTO.Response;

import com.hissah.Entities.Category;
import lombok.Data;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long parentCategoryId;
    private Boolean active;

    public static CategoryResponseDTO fromEntity(Category category) {
        if (category == null) return null;
        CategoryResponseDTO response = new CategoryResponseDTO();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setParentCategoryId(category.getParentCategoryId());
        response.setActive(category.getActive());
        return response;
    }
}
