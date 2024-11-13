package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
@Slf4j
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/expenses")
    public List<ExpenseDTO> getAllExpenses(Pageable page) {
        log.info("Fetching all categories with pagination: {}", page);
        return expenseService.getAllExpenses(page).toList();
    }

    @GetMapping("/expenses/{id}")
    public ExpenseDTO getExpenseById(@PathVariable Long id) {
        log.info("Request to fetch expense with ID: {}", id);
        return expenseService.getExpenseById(id);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<HttpStatus> deleteExpenseById(@PathVariable Long id) {
        log.info("Request to delete expense with ID: {}", id);
        expenseService.deleteExpenseById(id);
        log.info("Successfully deleted expense with ID: {}", id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/expenses")
    public ResponseEntity<HttpStatus> deleteAllExpensesForUser(Pageable pageable) {
        log.info("Request to delete all expenses for the user with pagination: {}", pageable);
        expenseService.deleteAllExpensesForUser(pageable);
        log.info("All expenses deleted successfully for the user");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/expenses")
    public ExpenseDTO saveExpenseDetails(@Valid @RequestBody ExpenseDTO expenseDTO) {
        log.info("Request to save expense details: {}", expenseDTO);
        return expenseService.saveExpanseDetails(expenseDTO);
    }

    @PutMapping("/expenses/{id}")
    public ExpenseDTO updateExpenseDetails(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        log.info("Request to update expense with ID: {} and details: {}", id, expenseDTO);
        return expenseService.updateExpenseDetails(id, expenseDTO);
    }

    @GetMapping("/expenses/category/{categoryName}")
    public List<ExpenseDTO> getExpensesByCategory(@PathVariable String categoryName) {
        log.info("Request to get expenses by category: {}", categoryName);
        return expenseService.getExpensesByCategoryName(categoryName);
    }

    @GetMapping("/expenses/user/category/{categoryName}")
    public List<ExpenseDTO> getCategoriesByNameForLoggedInUser(@PathVariable String categoryName){
        log.info("Request to get expenses for logged-in user by category: {}", categoryName);
        return expenseService.getCategoriesByNameForLoggedInUser(categoryName);
    }

    @GetMapping("/expenses/name")
    public List<ExpenseDTO> getAllExpensesByName(@RequestParam String keyword, Pageable page) {
        log.info("Request to get expenses by name containing keyword: {} with pagination: {}", keyword, page);
        return expenseService.readByName(keyword, page);
    }

    @GetMapping("/expenses/date")
    public List<ExpenseDTO> getAllExpensesByDate(@RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate, Pageable page) {
        log.info("Request to get expenses by date range from: {} to: {} with pagination: {}", startDate, endDate, page);
        return expenseService.readByDate(startDate, endDate, page);
    }
}