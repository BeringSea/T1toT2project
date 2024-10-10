package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseService {

    Page<Expense> getAllExpenses(Pageable page);

    Expense getExpenseById(Long id);

    void deleteExpenseById(Long id);

    Expense saveExpanseDetails(Expense expense);

    Expense updateExpenseDetails(Long id, Expense expense);
}
