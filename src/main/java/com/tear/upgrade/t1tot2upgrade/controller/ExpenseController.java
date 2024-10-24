package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

@RestController
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @GetMapping("/expenses")
    public List<ExpenseDTO> getAllExpenses(Pageable page) {
        return expenseService.getAllExpenses(page).toList();
    }

    @GetMapping("/expenses/{id}")
    public ExpenseDTO getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<HttpStatus> deleteExpenseById(@PathVariable Long id) {
        expenseService.deleteExpenseById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/expenses")
    public ResponseEntity<HttpStatus> deleteAllExpensesForUser(Pageable pageable) {
        expenseService.deleteAllExpensesForUser(pageable);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/expenses")
    public ExpenseDTO saveExpenseDetails(@Valid @RequestBody ExpenseDTO expenseDTO) {
        return expenseService.saveExpanseDetails(expenseDTO);
    }

    @PutMapping("/expenses/{id}")
    public ExpenseDTO updateExpenseDetails(@PathVariable Long id, @RequestBody ExpenseDTO expenseDTO) {
        return expenseService.updateExpenseDetails(id, expenseDTO);
    }

    @GetMapping("/expenses/category/{categoryName}")
    public List<ExpenseDTO> getExpensesByCategory(@PathVariable String categoryName) {
        return expenseService.getExpensesByCategoryName(categoryName);
    }

    @GetMapping("/expenses/user/category/{categoryName}")
    public List<ExpenseDTO> getCategoriesByNameForLoggedInUser(@PathVariable String categoryName){
        return expenseService.getCategoriesByNameForLoggedInUser(categoryName);
    }

    @GetMapping("/expenses/name")
    public List<ExpenseDTO> getAllExpensesByName(@RequestParam String keyword, Pageable page) {
        return expenseService.readByName(keyword, page);
    }

    @GetMapping("/expenses/date")
    public List<ExpenseDTO> getAllExpensesByDate(@RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate, Pageable page) {
        return expenseService.readByDate(startDate, endDate, page);
    }
}