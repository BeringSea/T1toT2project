package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
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
    public Page<CategoryDTO> getAllCategories(Pageable page) {
        Page<Category> categories = categoryRepository.findByUserId(userService.getLoggedInUser().getId(), page);
        return categories.map(this::convertToDTO);
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            throw new IllegalArgumentException("CategoryDTO cannot be null");
        }

        User loggedInUser = userService.getLoggedInUser();

        boolean exists = categoryRepository.existsByNameAndUserId(categoryDTO.getName(), loggedInUser.getId());
        if (exists) {
            throw new ItemAlreadyExistsException("Category with name: " + categoryDTO.getName() + " already exists");
        }

        Category category = new Category();
        category.setName(categoryDTO.getName());
        category.setDescription(categoryDTO.getDescription());
        category.setUser(loggedInUser);

        return convertToDTO(categoryRepository.save(category));
    }


    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existingCategory = getCategoryEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category is not found for id " + id));
        existingCategory.setName(categoryDTO.getName() != null ? categoryDTO.getName() : existingCategory.getName());
        existingCategory.setDescription(categoryDTO.getDescription() != null ? categoryDTO.getDescription() : existingCategory.getDescription());
        return convertToDTO(categoryRepository.save(existingCategory));
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

    private Optional<Category> getCategoryEntityById(Long id) {
        return categoryRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
    }

    private CategoryDTO convertToDTO(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category must not be null");
        }
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
