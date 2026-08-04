package com.hissah.DTO.Request;

import com.hissah.Entities.Category;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    private String name;
    private String description;
    private Long parentCategoryId;
    private Boolean active;

    public Category toEntity() {
        Category category = new Category();
        category.setName(this.name);
        category.setDescription(this.description);
        category.setParentCategoryId(this.parentCategoryId);
        category.setActive(this.active != null ? this.active : true);
        return category;
    }

    public void updateEntity(Category category) {
        if (category == null) {  return;
        }

        if (this.name != null) {
            category.setName(this.name);
        }

        if (this.description != null) {
            category.setDescription(this.description);
        }

        if (this.parentCategoryId != null) {
            category.setParentCategoryId(this.parentCategoryId);
        }

        if (this.active != null) {
            category.setActive(this.active);
        }
    }
}