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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    @Override
    public Page<ExpenseDTO> getAllExpenses(Pageable page) {
        Page<Expense> expenses = expenseRepository.findByUserId(userService.getLoggedInUser().getId(), page);
        return expenses.map(this::convertToDTO);
    }

    @Override
    public ExpenseDTO getExpenseById(Long id) {
        Optional<Expense> expense = getExpenseEntityById(id);
        if (expense.isPresent()) {
            return convertToDTO(expense.get());
        }
        throw new ResourceNotFoundException("Expense is not found for id " + id);
    }

    @Override
    public void deleteExpenseById(Long id) {
        Optional<Expense> expenseOptional = expenseRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
        if (expenseOptional.isPresent()) {
            expenseRepository.delete(expenseOptional.get());
        } else {
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
                expenseRepository.deleteAll(expensesPage.getContent());
            }
            pageable = pageable.next();
        } while (expensesPage.hasNext());

        if (expensesPage.getTotalElements() == 0) {
            throw new ResourceNotFoundException("No expenses found for user " + loggedInUser.getId());
        }
    }

    @Override
    public ExpenseDTO saveExpanseDetails(ExpenseDTO expenseDTO) {
        User loggedInUser = userService.getLoggedInUser();

        Category category = null;
        if (expenseDTO.getCategoryDTO() != null && expenseDTO.getCategoryDTO().getName() != null) {
            Optional<Category> optionalCategory = categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), loggedInUser);

            if (optionalCategory.isPresent()) {
                category = optionalCategory.get();
            } else {
                category = new Category();
                category.setName(expenseDTO.getCategoryDTO().getName());
                category.setDescription(expenseDTO.getCategoryDTO().getDescription());
                category.setUser(loggedInUser);
                category = categoryRepository.save(category);
            }
        } else {
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
                .orElseThrow(() -> new ResourceNotFoundException("Expense is not found for id " + id));

        if (expenseDTO.getCategoryDTO() != null && expenseDTO.getCategoryDTO().getName() != null) {
            User loggedInUser = userService.getLoggedInUser();

            Optional<Category> optionalCategory = categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), loggedInUser);

            Category category = optionalCategory.orElseThrow(() ->
                    new ResourceNotFoundException("Category not found for name: " + expenseDTO.getCategoryDTO().getName()));

            category.setDescription(expenseDTO.getCategoryDTO().getDescription() != null ? expenseDTO.getCategoryDTO().getDescription() : category.getDescription());
            categoryRepository.save(category);
            existingExpense.setCategory(category);
        }

        return convertToDTO(expenseRepository.save(existingExpense));
    }

    public List<ExpenseDTO> readByName(String name, Pageable page) {
        List<Expense> expenses = expenseRepository.findByUserIdAndNameContaining(userService.getLoggedInUser().getId(), name, page).toList();
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

        return expenses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDTO> getExpensesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + categoryName));
        List<Expense> expenses = expenseRepository.findByCategory(category);
        return expenses.stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Override
    public List<ExpenseDTO> getCategoriesByNameForLoggedInUser(String categoryName) {
        User loggedInUser = userService.getLoggedInUser();

        Category category = categoryRepository.findByNameAndUser(categoryName, loggedInUser)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + categoryName));

        List<Expense> expenses = expenseRepository.findByUserAndCategory(loggedInUser, category);
        return expenses.stream()
                .map(this::convertToDTO)
                .toList();
    }

    private Optional<Expense> getExpenseEntityById(Long id) {
        return expenseRepository.findByUserIdAndId(userService.getLoggedInUser().getId(), id);
    }

    private ExpenseDTO convertToDTO(Expense expense) {

        CategoryDTO categoryDTO = null;
        if (expense.getCategory() != null) {
            categoryDTO = new CategoryDTO(
                    expense.getCategory().getId(),
                    expense.getCategory().getName(),
                    expense.getCategory().getDescription()
            );
        }

        return new ExpenseDTO(
                expense.getId(),
                expense.getName(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getNotes(),
                categoryDTO
        );
    }
}
