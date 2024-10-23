package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<CategoryDTO> getAllExpenses(Pageable page);

    CategoryDTO saveCategory(CategoryDTO categoryDTO);

    void deleteCategoryById(Long id);

    void deleteAllCategoriesForUser(Pageable pageable);
}
