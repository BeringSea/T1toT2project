package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories(Pageable page) {
        return categoryService.getAllExpenses(page).toList();
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<HttpStatus> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategoryById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/categories")
    public CategoryDTO saveCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        return categoryService.saveCategory(categoryDTO);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/categories")
    public ResponseEntity<HttpStatus> deleteAllCategoriesForUser(Pageable pageable) {
        categoryService.deleteAllCategoriesForUser(pageable);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}