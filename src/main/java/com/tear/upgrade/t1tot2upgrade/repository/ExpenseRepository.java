package com.tear.upgrade.t1tot2upgrade.repository;

import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
