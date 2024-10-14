package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.Expense;
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
    public List<Expense> getAllExpenses(Pageable page) {
        return expenseService.getAllExpenses(page).toList();
    }

    @GetMapping("/expenses/{id}")
    public Expense getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/expenses")
    public ResponseEntity<HttpStatus> deleteExpenseById(@RequestParam Long id) {
        expenseService.deleteExpenseById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/expenses")
    public Expense saveExpenseDetails(@Valid @RequestBody Expense expense) {
        return expenseService.saveExpanseDetails(expense);
    }

    @PutMapping("/expenses/{id}")
    public Expense updateExpenseDetails(@PathVariable Long id, @RequestBody Expense expense) {
        return expenseService.updateExpenseDetails(id, expense);
    }

//    @GetMapping("/expenses/category")
//    public List<Expense> getExpensesByCategory(@RequestParam String category, Pageable page) {
//        return expenseService.readByCategory(category, page);
//    }

    @GetMapping("/expenses/name")
    public List<Expense> getAllExpensesByName(@RequestParam String keyword, Pageable page) {
        return expenseService.readByName(keyword, page);
    }

    @GetMapping("/expenses/date")
    public List<Expense> getAllExpensesByDate(@RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate, Pageable page) {
        return expenseService.readByDate(startDate, endDate, page);
    }
}