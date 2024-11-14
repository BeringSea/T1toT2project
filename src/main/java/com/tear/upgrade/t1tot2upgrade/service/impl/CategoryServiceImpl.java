package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.CategoryRepository;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Page<CategoryDTO> getAllCategories(Pageable page) {
        log.debug("Fetching categories for user ID: {}", userService.getLoggedInUser().getId());
        Page<Category> categories = categoryRepository.findByUserId(userService.getLoggedInUser().getId(), page);
        log.debug("Fetched {} categories for user ID: {}", categories.getTotalElements(), userService.getLoggedInUser().getId());
        return categories.map(this::convertToDTO);
    }

    @Override
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        if (categoryDTO == null) {
            log.error("CategoryDTO is null");
            throw new IllegalArgumentException("CategoryDTO cannot be null");
        }

        User loggedInUser = userService.getLoggedInUser();
        log.debug("Logged in user ID: {}", loggedInUser.getId());

        boolean exists = categoryRepository.existsByNameAndUserId(categoryDTO.getName(), loggedInUser.getId());
        if (exists) {
            log.error("Category with name '{}' already exists for user ID: {}", categoryDTO.getName(), loggedInUser.getId());
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
                .orElseThrow(() -> {
                    log.error("Category with ID '{}' not found", id);
                    return new ResourceNotFoundException("Category is not found for id " + id);
                });
        existingCategory.setName(categoryDTO.getName() != null ? categoryDTO.getName() : existingCategory.getName());
        existingCategory.setDescription(categoryDTO.getDescription() != null ? categoryDTO.getDescription() : existingCategory.getDescription());
        return convertToDTO(categoryRepository.save(existingCategory));
    }


    @Override
    public void deleteCategoryById(Long id) {
        Optional<Category> categoryOptional = categoryRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
        if (categoryOptional.isPresent()) {
            categoryRepository.delete(categoryOptional.get());
            log.info("Category with ID '{}' deleted successfully", id);
        } else {
            log.error("Category with ID '{}' not found for deletion", id);
            throw new ResourceNotFoundException("Category is not found for id " + id);
        }
    }

    @Override
    public void deleteAllCategoriesForUser(Pageable pageable) {
        if (pageable == null) {
            log.error("Pageable is null");
            throw new IllegalArgumentException("Pageable must not be null");
        }

        User loggedInUser = userService.getLoggedInUser();
        log.debug("Attempting to delete all categories for user ID: {}", loggedInUser.getId());

        Page<Category> categoriesPage;
        do {
            categoriesPage = categoryRepository.findByUserId(loggedInUser.getId(), pageable);
            if (!categoriesPage.isEmpty()) {
                log.debug("Deleting {} categories for user ID: {}", categoriesPage.getContent().size(), loggedInUser.getId());
                categoryRepository.deleteAll(categoriesPage.getContent());
            }
            pageable = pageable.next();
        } while (categoriesPage.hasNext());

        if (categoriesPage.getTotalElements() == 0) {
            log.error("No categories found for user ID: {}", loggedInUser.getId());
            throw new ResourceNotFoundException("No categories found for user " + loggedInUser.getId());
        }
        log.debug("All categories deleted for user ID: {}", loggedInUser.getId());
    }

    private Optional<Category> getCategoryEntityById(Long id) {
        return categoryRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
    }

    private CategoryDTO convertToDTO(Category category) {
        if (category == null) {
            log.error("Category is null, cannot convert to DTO");
            throw new IllegalArgumentException("Category must not be null");
        }
        log.debug("Converting category with ID: {} to DTO", category.getId());
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }
}
