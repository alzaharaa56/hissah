package com.hissah.Services.Implementations;

import com.hissah.DTO.Request.CategoryRequestDTO;
import com.hissah.DTO.Response.CategoryResponseDTO;
import com.hissah.Entities.Category;
import com.hissah.Exceptions.BusinessRuleException;
import com.hissah.Exceptions.DuplicateResourceException;
import com.hissah.Exceptions.ResourceNotFoundException;
import com.hissah.Repositories.CategoryRepository;
import com.hissah.Services.CategoryService;
import com.hissah.Services.Implementations.Support.ServiceDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ServiceDtoMapper mapper;

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        String name = required(
                mapper.text(request, "name", "categoryName"),
                "Category name"
        );

        ensureNameAvailable(name, null);

        Category category = new Category();
        category.setName(name);
        category.setActive(true);

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO update(
            Long categoryId,
            CategoryRequestDTO request
    ) {
        Category category = getCategory(categoryId);

        String name = mapper.text(
                request,
                "name",
                "categoryName"
        );

        if (name != null
                && !name.equalsIgnoreCase(category.getName())) {
            ensureNameAvailable(name, categoryId);
            category.setName(name);
        }

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponseDTO deactivate(Long categoryId) {
        Category category = getCategory(categoryId);
        category.setActive(false);
        return toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponseDTO getById(Long categoryId) {
        return toResponse(getCategory(categoryId));
    }

    @Override
    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findAll()
                .stream()
                .filter(category ->
                        Boolean.TRUE.equals(category.getActive())
                )
                .sorted(
                        Comparator.comparing(
                                Category::getName,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .map(this::toResponse)
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
                .map(this::toResponse)
                .toList();
    }

    private void ensureNameAvailable(
            String name,
            Long excludedCategoryId
    ) {
        boolean duplicate = categoryRepository.findAll()
                .stream()
                .anyMatch(category ->
                        (
                                excludedCategoryId == null
                                        || !excludedCategoryId.equals(
                                        category.getId()
                                )
                        )
                                && category.getName() != null
                                && category.getName()
                                .equalsIgnoreCase(name)
                );

        if (duplicate) {
            throw new DuplicateResourceException(
                    "A category with this name already exists."
            );
        }
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
    }

    private CategoryResponseDTO toResponse(Category category) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("id", category.getId());
        values.put("name", category.getName());
        values.put("active", category.getActive());
        values.put("createdAt", LocalDateTime.now());
        values.put("updatedAt", LocalDateTime.now());
        return mapper.toDto(values, CategoryResponseDTO.class);
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BusinessRuleException(
                    fieldName + " is required."
            );
        }
        return value.trim();
    }
}