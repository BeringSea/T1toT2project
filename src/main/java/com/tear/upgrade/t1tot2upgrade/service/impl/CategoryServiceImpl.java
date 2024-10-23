package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.CategoryRepository;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDTO> getAllExpenses(Pageable page) {
        Page<Category> categories = categoryRepository.findByUserId(userService.getLoggedInUser().getId(), page);
        return categories.map(this::convertToDTO);
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            throw new IllegalArgumentException("ExpenseDTO cannot be null");
        }
        User loggedInUser = userService.getLoggedInUser();
        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setUser(loggedInUser);

        Category savedCategory = categoryRepository.save(category);

        return convertToDTO(savedCategory);
    }

    @Override
    public void deleteCategoryById(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
        if (categoryOptional.isPresent()) {
            categoryRepository.delete(categoryOptional.get());
        } else {
            throw new ResourceNotFoundException("Category is not found for id " + id);
        }
    }

    @Override
    public void deleteAllCategoriesForUser(Pageable pageable) {
        User loggedInUser = userService.getLoggedInUser();

        Page<Category> categoriesPage;
        do {
            categoriesPage = categoryRepository.findByUserId(loggedInUser.getId(), pageable);
            if (!categoriesPage.isEmpty()) {
                categoryRepository.deleteAll(categoriesPage.getContent());
            }
            pageable = pageable.next();
        } while (categoriesPage.hasNext());

        if (categoriesPage.getTotalElements() == 0) {
            throw new ResourceNotFoundException("No expenses found for user " + loggedInUser.getId());
        }
    }

    private CategoryDTO convertToDTO(Category category) {
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
