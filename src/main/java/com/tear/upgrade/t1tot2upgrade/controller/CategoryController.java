package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryDTO> getAllExpenses(Pageable page) {
        return categoryService.getAllExpenses(page).toList();
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/categories")
    public CategoryDTO saveExpenseDetails(@Valid @RequestBody CategoryDTO categoryDTO) {
        return categoryService.saveCategory(categoryDTO);
    }
}