package com.hissah.Controllers;
import com.hissah.DTO.Request.CategoryRequestDTO;
import com.hissah.DTO.Response.CategoryResponseDTO;
import com.hissah.Services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getActiveCategories() {
        List<CategoryResponseDTO> response = categoryService.getActiveCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> response = categoryService.getAllCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoryResponseDTO response = categoryService.getById(categoryId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.create(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryRequestDTO request
    ) {
        CategoryResponseDTO response = categoryService.update(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/deactivate")
    public ResponseEntity<CategoryResponseDTO> deactivateCategory(@PathVariable Long categoryId) {
        CategoryResponseDTO response = categoryService.deactivate(categoryId);
        return ResponseEntity.ok(response);
    }
}
