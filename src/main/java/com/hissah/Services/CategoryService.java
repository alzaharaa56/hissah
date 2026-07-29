package com.hissah.Services;

import com.hissah.DTO.Response.CategoryResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;


    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findCategoriesByActiveStatus(true).stream()
                .map(CategoryResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }


    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        return CategoryResponseDTO.fromEntity(category);
    }

    @Transactional
    public CategoryResponseDTO createCategory(Category category) {
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.fromEntity(savedCategory);
    }


    @Transactional
    public CategoryResponseDTO updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        category.setName(categoryDetails.getName());
        category.setParentCategoryId(categoryDetails.getParentCategoryId());
        category.setActive(categoryDetails.getActive());

        Category updatedCategory = categoryRepository.save(category);
        return CategoryResponseDTO.fromEntity(updatedCategory);
    }


    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.delete(category);
    }
}