package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.CategoryRequestDTO;
import com.hissah.DTO.Response.CategoryResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        String name = required(request.getName(), "Category name");
        ensureNameAvailable(name, null);
        validateParentCategory(request.getParentCategoryId(), null);

        Category category = new Category();
        category.setName(name);
        category.setDescription(trimToNull(request.getDescription()));
        category.setParentCategoryId(request.getParentCategoryId());
        category.setActive(
                request.getActive() != null
                        ? request.getActive()
                        : true
        );

        return CategoryResponseDTO.fromEntity(
                categoryRepository.save(category)
        );
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(
            Long categoryId,
            CategoryRequestDTO request
    ) {
        Category category = getCategory(categoryId);

        if (request.getName() != null
                && !request.getName().isBlank()
                && !request.getName().trim().equalsIgnoreCase(category.getName())) {
            String newName = request.getName().trim();
            ensureNameAvailable(newName, categoryId);
            category.setName(newName);
        }

        if (request.getDescription() != null) {
            category.setDescription(trimToNull(request.getDescription()));
        }

        if (request.getParentCategoryId() != null) {
            validateParentCategory(request.getParentCategoryId(), categoryId);
            category.setParentCategoryId(request.getParentCategoryId());
        }

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return CategoryResponseDTO.fromEntity(
                categoryRepository.save(category)
        );
    }

    @Override
    @Transactional
    public CategoryResponseDTO deactivate(Long categoryId) {
        Category category = getCategory(categoryId);
        category.setActive(false);

        return CategoryResponseDTO.fromEntity(
                categoryRepository.save(category)
        );
    }

    @Override
    public CategoryResponseDTO getById(Long categoryId) {
        return CategoryResponseDTO.fromEntity(
                getCategory(categoryId)
        );
    }

    @Override
    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findAll()
                .stream()
                .filter(category -> Boolean.TRUE.equals(category.getActive()))
                .sorted(
                        Comparator.comparing(
                                Category::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Category::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    private void ensureNameAvailable(
            String name,
            Long excludedCategoryId
    ) {
        boolean duplicate = categoryRepository.findAll()
                .stream()
                .anyMatch(category ->
                        (excludedCategoryId == null
                                || !excludedCategoryId.equals(category.getId()))
                                && category.getName() != null
                                && category.getName().equalsIgnoreCase(name)
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "A category with this name already exists."
            );
        }
    }

    private void validateParentCategory(
            Long parentCategoryId,
            Long currentCategoryId
    ) {
        if (parentCategoryId == null) {
            return;
        }

        if (currentCategoryId != null
                && currentCategoryId.equals(parentCategoryId)) {
            throw new BusinessRuleException(
                    "A category cannot be its own parent."
            );
        }

        categoryRepository.findById(parentCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Parent category not found with id: " + parentCategoryId
                ));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    fieldName + " is required."
            );
        }

        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
