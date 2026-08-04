package com.hissah.Controllers;

import com.hissah.DTO.Request.CategoryRequestDTO;
import com.hissah.DTO.Response.CategoryResponseDTO;
import com.hissah.Services.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> create(
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.create(request));
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> update(
            @PathVariable @Positive Long categoryId,
            @Valid @RequestBody CategoryRequestDTO request
    ) {
        return ResponseEntity.ok(
                categoryService.update(categoryId, request)
        );
    }

    @PatchMapping("/{categoryId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponseDTO> deactivate(
            @PathVariable @Positive Long categoryId
    ) {
        return ResponseEntity.ok(
                categoryService.deactivate(categoryId)
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getById(
            @PathVariable @Positive Long categoryId
    ) {
        return ResponseEntity.ok(
                categoryService.getById(categoryId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getActive() {
        return ResponseEntity.ok(
                categoryService.getActiveCategories()
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }
}
