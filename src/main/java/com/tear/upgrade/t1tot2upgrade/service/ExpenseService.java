package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.List;

public interface ExpenseService {

    Page<ExpenseDTO> getAllExpenses(Pageable page);

    ExpenseDTO getExpenseById(Long id);

    void deleteExpenseById(Long id);

    void deleteAllExpensesForUser(Pageable pageable);

    ExpenseDTO saveExpanseDetails(ExpenseDTO expense);

    ExpenseDTO updateExpenseDetails(Long id, ExpenseDTO expenseDTO);

    List<ExpenseDTO> readByName(String name, Pageable page);

    List<ExpenseDTO> readByDate(Date startDate, Date endDate, Pageable page);
}
