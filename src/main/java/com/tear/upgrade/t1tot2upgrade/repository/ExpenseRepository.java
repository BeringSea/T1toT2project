package com.tear.upgrade.t1tot2upgrade.repository;

import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Page<Expense> findByNameContaining(String keyword, Pageable page);

    Page<Expense> findByDateBetween(Date startDate, Date endDate, Pageable page);

    Page<Expense> findByUserId(Long userId, Pageable page);

    Optional<Expense> findByUserIdAndId(Long userId, Long expenseId);
}
