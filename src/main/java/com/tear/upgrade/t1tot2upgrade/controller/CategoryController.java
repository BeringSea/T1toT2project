package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories(Pageable page) {
        log.info("Fetching all categories with pagination: {}", page);
        return categoryService.getAllCategories(page).toList();
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<HttpStatus> deleteCategoryById(@PathVariable Long id) {
        log.info("Deleting category with ID: {}", id);
        categoryService.deleteCategoryById(id);
        log.info("Category with ID: {} deleted successfully", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/categories")
    public CategoryDTO saveCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("Saving new category: {}", categoryDTO);
        return categoryService.saveCategory(categoryDTO);
    }

    @PutMapping("/categories/{id}")
    public CategoryDTO updateExpenseDetails(@PathVariable Long id, @RequestBody CategoryDTO categoryDTO) {
        log.info("Updating category with ID: {}. New data: {}", id, categoryDTO);
        return categoryService.updateCategory(id, categoryDTO);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories")
    public ResponseEntity<HttpStatus> deleteAllCategoriesForUser(Pageable pageable) {
        log.info("Deleting all categories for user with pagination: {}", pageable);
        categoryService.deleteAllCategoriesForUser(pageable);
        log.info("All categories deleted successfully");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}