package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    /**
     * Retrieves a paginated list of all expenses for logged-in user.
     *
     * @param pageable the pagination information (page number, size, sorting)
     * @return a paginated list of {@link CategoryDTO} representing all expense categories
     */
    Page<CategoryDTO> getAllExpenses(Pageable pageable);

    /**
     * Saves a new category
     *
     * @param categoryDTO the category data to be saved
     * @return the saved {@link CategoryDTO} instance
     */
    CategoryDTO saveCategory(CategoryDTO categoryDTO);

    /**
     * Deletes a category by its identifier.
     *
     * @param id the identifier of the category to delete
     */
    void deleteCategoryById(Long id);

    /**
     * Deletes all categories associated with a user.
     *
     * @param pageable the pagination information to handle potentially large deletions
     */
    void deleteAllCategoriesForUser(Pageable pageable);
}
