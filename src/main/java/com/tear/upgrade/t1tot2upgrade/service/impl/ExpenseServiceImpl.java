package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import com.tear.upgrade.t1tot2upgrade.repository.ExpenseRepository;
import com.tear.upgrade.t1tot2upgrade.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }
}
