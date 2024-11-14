package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.CategoryDTO;
import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.CategoryRepository;
import com.tear.upgrade.t1tot2upgrade.repository.ExpenseRepository;
import com.tear.upgrade.t1tot2upgrade.service.ExpenseService;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Override
    public Page<ExpenseDTO> getAllExpenses(Pageable page) {
        if (page == null) {
            log.error("Pageable is null");
            throw new IllegalArgumentException("Pageable cannot be null");
        }
        log.debug("Fetching expenses for user ID: {} with pageable: {}", userService.getLoggedInUser().getId(), page);
        Page<Expense> expenses = expenseRepository.findByUserId(userService.getLoggedInUser().getId(), page);
        log.info("Fetched {} expenses for user ID: {}", expenses.getTotalElements(), userService.getLoggedInUser().getId());
        return expenses.map(this::convertToDTO);
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        Optional<Expense> expense = getExpenseEntityById(id);
        if (expense.isPresent()) {
            log.debug("Found expense with ID: {}", id);
            return convertToDTO(expense.get());
        } else {
            log.error("Expense with ID '{}' not found. Throwing ResourceNotFoundException.", id);
            throw new ResourceNotFoundException("Expense is not found for id " + id);
        }
    }

    @Override
    public void deleteExpenseById(Long id) {
        Optional<Expense> expenseOptional = expenseRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
        if (expenseOptional.isPresent()) {
            expenseRepository.delete(expenseOptional.get());
            log.info("Expense with ID '{}' deleted successfully", id);
        } else {
            log.error("Expense with ID '{}' not found for deletion", id);
            throw new ResourceNotFoundException("Expense is not found for id " + id);
        }
    }

    @Override
    public void deleteAllExpensesForUser(Pageable pageable) {
        User loggedInUser = userService.getLoggedInUser();

        Page<Expense> expensesPage;
        do {
            expensesPage = expenseRepository.findByUserId(loggedInUser.getId(), pageable);
            if (!expensesPage.isEmpty()) {
                log.debug("Deleting {} expenses for user ID: {}", expensesPage.getContent().size(), loggedInUser.getId());
                expenseRepository.deleteAll(expensesPage.getContent());
            }
            pageable = pageable.next();
        } while (expensesPage.hasNext());

        if (expensesPage.getTotalElements() == 0) {
            log.error("No expenses found for user ID: {}", loggedInUser.getId());
            throw new ResourceNotFoundException("No expenses found for user " + loggedInUser.getId());
        }
        log.info("All expenses deleted for user ID: {}", loggedInUser.getId());
    }

    @Override
    public ExpenseDTO saveExpanseDetails(ExpenseDTO expenseDTO) {
        User loggedInUser = userService.getLoggedInUser();

        Category category = null;
        if (expenseDTO.getCategoryDTO() != null && expenseDTO.getCategoryDTO().getName() != null) {
            Optional<Category> optionalCategory = categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), loggedInUser);

            if (optionalCategory.isPresent()) {
                category = optionalCategory.get();
                log.debug("Category '{}' found for user ID: {}", expenseDTO.getCategoryDTO().getName(), loggedInUser.getId());
            } else {
                log.debug("Category '{}' not found. Creating new category.", expenseDTO.getCategoryDTO().getName());
                category = new Category();
                category.setName(expenseDTO.getCategoryDTO().getName());
                category.setDescription(expenseDTO.getCategoryDTO().getDescription());
                category.setUser(loggedInUser);
                category = categoryRepository.save(category);
                log.debug("New category '{}' created and saved", expenseDTO.getCategoryDTO().getName());
            }
        } else {
            log.error("Category name must be provided to add an expense.");
            throw new IllegalArgumentException("Category name must be provided to add an expense.");
        }

        Expense expense = new Expense();
        expense.setName(expenseDTO.getName());
        expense.setDescription(expenseDTO.getDescription());
        expense.setAmount(expenseDTO.getAmount());
        expense.setDate(expenseDTO.getDate());
        expense.setNotes(expenseDTO.getNotes());
        expense.setUser(loggedInUser);
        expense.setCategory(category);

        return convertToDTO(expenseRepository.save(expense));
    }

    @Override
    public ExpenseDTO updateExpenseDetails(Long id, ExpenseDTO expenseDTO) {

        Expense existingExpense = getExpenseEntityById(id)
                .orElseThrow(() -> {
                    log.error("Expense with ID '{}' not found", id);
                    return new ResourceNotFoundException("Expense is not found for id " + id);
                });

        if (expenseDTO.getCategoryDTO() != null && expenseDTO.getCategoryDTO().getName() != null) {
            User loggedInUser = userService.getLoggedInUser();
            Optional<Category> optionalCategory = categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), loggedInUser);

            if (optionalCategory.isEmpty()) {
                log.error("Category with name '{}' not found for user ID: {}", expenseDTO.getCategoryDTO().getName(), loggedInUser.getId());
                throw new ResourceNotFoundException("Category not found for name: " + expenseDTO.getCategoryDTO().getName());
            }

            Category category = optionalCategory.get();
            category.setDescription(expenseDTO.getCategoryDTO().getDescription() != null ? expenseDTO.getCategoryDTO().getDescription() : category.getDescription());
            categoryRepository.save(category);
            log.info("Category '{}' updated successfully for expense with ID: {}", expenseDTO.getCategoryDTO().getName(), id);
            existingExpense.setCategory(category);
        }

        return convertToDTO(expenseRepository.save(existingExpense));
    }

    @Override
    public List<ExpenseDTO> readByName(String name, Pageable page) {
        List<Expense> expenses = expenseRepository.findByUserIdAndNameContaining(userService.getLoggedInUser().getId(), name, page).toList();
        log.debug("Found {} expenses with name containing '{}'", expenses.size(), name);
        return expenses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> readByDate(Date startDate, Date endDate, Pageable page) {
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date(System.currentTimeMillis());
        }

        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(
                userService.getLoggedInUser().getId(), startDate, endDate, page).toList();
        log.debug("Found {} expenses between dates: {} and {}", expenses.size(), startDate, endDate);

        return expenses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> {
                    log.error("Category '{}' not found", categoryName);
                    return new ResourceNotFoundException("Category not found with name: " + categoryName);
                });
        List<Expense> expenses = expenseRepository.findByCategory(category);
        log.info("Found {} expenses for category name: {}", expenses.size(), categoryName);
        return expenses.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<ExpenseDTO> getCategoriesByNameForLoggedInUser(String categoryName) {
        User loggedInUser = userService.getLoggedInUser();

        Category category = categoryRepository.findByNameAndUser(categoryName, loggedInUser)
                .orElseThrow(() -> {
                    log.error("Category '{}' not found for user ID: {}", categoryName, loggedInUser.getId());
                    return new ResourceNotFoundException("Category not found with name: " + categoryName);
                });

        List<Expense> expenses = expenseRepository.findByUserAndCategory(loggedInUser, category);
        log.info("Found {} expenses for user ID: {} and category '{}'", expenses.size(), loggedInUser.getId(), categoryName);
        return expenses.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private Optional<Expense> getExpenseEntityById(Long id) {
        return expenseRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
    }

    private ExpenseDTO convertToDTO(Expense expense) {
        if (expense == null) {
            log.error("Expense is null, cannot convert to DTO");
            throw new IllegalArgumentException("Expense must not be null");
        }

        CategoryDTO categoryDTO = null;
        if (expense.getCategory() != null) {
            categoryDTO = CategoryDTO.builder()
                    .id(expense.getCategory().getId())
                    .name(expense.getCategory().getName())
                    .description(expense.getCategory().getDescription())
                    .build();
        }
        log.debug("Converting expense with ID: {} to DTO", expense.getId());
        return ExpenseDTO.builder()
                .id(expense.getId())
                .name(expense.getName())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .date(expense.getDate())
                .notes(expense.getNotes())
                .categoryDTO(categoryDTO)
                .build();
    }
}
