package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Date;
import java.util.List;

public interface ExpenseService {

    /**
     * Retrieves a paginated list of all expenses for logged-in user.
     *
     * @param page the pagination information (page number, size, sorting)
     * @return a paginated list of {@link ExpenseDTO} representing all expenses
     */
    Page<ExpenseDTO> getAllExpenses(Pageable page);

    /**
     * Retrieves an expense by its identifier.
     *
     * @param id the identifier of the expense
     * @return the {@link ExpenseDTO} corresponding to the given id
     */
    ExpenseDTO getExpenseById(Long id);

    /**
     * Deletes an expense by its identifier.
     *
     * @param id the identifier of the expense to delete
     */
    void deleteExpenseById(Long id);

    /**
     * Deletes all expenses associated with a user.
     *
     * @param pageable the pagination information to handle potentially large deletions
     */
    void deleteAllExpensesForUser(Pageable pageable);

    /**
     * Saves the details of a new expense or updates an existing one.
     *
     * @param expense the expense data to be saved
     * @return the saved {@link ExpenseDTO} instance
     */
    ExpenseDTO saveExpanseDetails(ExpenseDTO expense);

    /**
     * Updates the details of an existing expense.
     *
     * @param id         the identifier of the expense to update
     * @param expenseDTO the updated expense data
     * @return the updated {@link ExpenseDTO} instance
     */
    ExpenseDTO updateExpenseDetails(Long id, ExpenseDTO expenseDTO);

    /**
     * Retrieves a list of expenses by expense name.
     *
     * @param name the name of the expense to search for
     * @param page the pagination information (page number, size, sorting)
     * @return a list of {@link ExpenseDTO} matching the given name
     */
    List<ExpenseDTO> readByName(String name, Pageable page);

    /**
     * Retrieves a list of expenses within a specified date range.
     *
     * @param startDate the start date for the range
     * @param endDate   the end date for the range
     * @param page      the pagination information (page number, size, sorting)
     * @return a list of {@link ExpenseDTO} within the specified date range
     */
    List<ExpenseDTO> readByDate(Date startDate, Date endDate, Pageable page);
}
