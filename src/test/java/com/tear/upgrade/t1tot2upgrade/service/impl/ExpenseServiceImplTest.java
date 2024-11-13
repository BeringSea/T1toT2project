package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.CategoryRepository;
import com.tear.upgrade.t1tot2upgrade.repository.ExpenseRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ExpenseServiceImplTest {

    public static final long ID_VALUE = 1L;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Mock
    private UserService userService;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private User mockUser;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = mock(User.class);
        mockUser.setId(1L);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        objectMapper = new ObjectMapper();
    }

    @Test
    void whenUserLoggedInThenGetAllCategoriesSuccess() throws IOException {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        String validMessagesArray = FileHelper.readFromFile("requests/expense/ExpenseArray.json");
        List<Expense> expenses = Arrays.asList(objectMapper.readValue(validMessagesArray, Expense[].class));

        // when
        when(expenseRepository.findByUserId(anyLong(), any(Pageable.class))).thenReturn(new PageImpl<>(expenses));
        Page<ExpenseDTO> result = expenseService.getAllExpenses(pageable);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getContent().size()),
                () -> {
                    ExpenseDTO expenseDTO1 = result.getContent().get(0);
                    assertThat(expenseDTO1).isNotNull();
                    assertThat(expenseDTO1.getId()).isEqualTo(ID_VALUE);
                    assertThat(expenseDTO1.getName()).isEqualTo("Expense 1");
                    assertThat(expenseDTO1.getDescription()).isEqualTo("Description for expense 1");
                    assertThat(expenseDTO1.getAmount()).isEqualByComparingTo(new BigDecimal("100.0"));
                    assertThat(expenseDTO1.getDate()).isEqualTo("2024-10-31T00:00:00.000Z");
                    assertThat(expenseDTO1.getCategoryDTO()).isNotNull();
                    assertThat(expenseDTO1.getCategoryDTO().getId()).isEqualTo(1L);
                    assertThat(expenseDTO1.getCategoryDTO().getName()).isEqualTo("Category 1");
                    assertThat(expenseDTO1.getCategoryDTO().getDescription()).isEqualTo("Description for category 1");
                },
                () -> {
                    ExpenseDTO expenseDTO2 = result.getContent().get(1);
                    assertThat(expenseDTO2).isNotNull();
                    assertThat(expenseDTO2.getId()).isEqualTo(2L);
                    assertThat(expenseDTO2.getName()).isEqualTo("Expense 2");
                    assertThat(expenseDTO2.getDescription()).isEqualTo("Description for expense 2");
                    assertThat(expenseDTO2.getAmount()).isEqualByComparingTo(new BigDecimal("200.5"));
                    assertThat(expenseDTO2.getDate()).isEqualTo("2024-11-05T00:00:00.000Z");
                    assertThat(expenseDTO2.getCategoryDTO()).isNotNull();
                    assertThat(expenseDTO2.getCategoryDTO().getId()).isEqualTo(2L);
                    assertThat(expenseDTO2.getCategoryDTO().getName()).isEqualTo("Category 2");
                    assertThat(expenseDTO2.getCategoryDTO().getDescription()).isEqualTo("Description for category 2");
                }
        );
    }

    @Test
    void whenUserLoggedInThenGetExpenseByIdSuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, ID_VALUE)).thenReturn(Optional.of(expense));
        ExpenseDTO result = expenseService.getExpenseById(expense.getId());

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(expense.getId(), result.getId()),
                () -> assertEquals("Expense 1", result.getName()),
                () -> assertEquals("Description for expense 1", result.getDescription()),
                () -> assertEquals(new BigDecimal("100.00"), result.getAmount()),
                () -> assertEquals(expense.getDate(), result.getDate()),
                () -> assertEquals("Notes for expense 1", result.getNotes()),
                () -> {
                    assertThat(result.getCategoryDTO()).isNotNull();
                    assertThat(result.getCategoryDTO().getId()).isEqualTo(ID_VALUE);
                    assertThat(result.getCategoryDTO().getName()).isEqualTo("Category 1");
                    assertThat(result.getCategoryDTO().getDescription()).isEqualTo("Description for category 1");
                });
    }

    @Test
    void whenExpenseExistsForLoggedInUserThenDeleteExpenseSuccessfully() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        Long expenseId = expense.getId();

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, expenseId)).thenReturn(Optional.of(expense));
        expenseService.deleteExpenseById(expenseId);

        // then
        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void whenExpensesExistForLoggedInUserThenDeleteAllExpensesForUserSuccessfully() throws IOException {
        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensesPage = new PageImpl<>(List.of(expense));

        // when
        when(expenseRepository.findByUserId(ID_VALUE, pageable)).thenReturn(expensesPage);
        expenseService.deleteAllExpensesForUser(pageable);

        // then
        verify(expenseRepository, times(1)).deleteAll(expensesPage.getContent());
    }

    @Test
    void whenValidExpenseDTOForLoggedInUserThenSaveExpenseSuccessfully() throws IOException {
        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);


        // when
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        ExpenseDTO result = expenseService.saveExpanseDetails(expenseDTO);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(expense.getId(), result.getId()),
                () -> assertEquals(expense.getName(), result.getName()),
                () -> assertEquals(expense.getDescription(), result.getDescription()),
                () -> assertEquals(expense.getAmount(), result.getAmount()),
                () -> assertEquals(expense.getDate(), result.getDate()),
                () -> assertEquals(expense.getNotes(), result.getNotes()),
                () -> assertEquals(expense.getId(), result.getCategoryDTO().getId()),
                () -> assertEquals(expense.getCategory().getName(), result.getCategoryDTO().getName())
        );
    }

    @Test
    void whenValidExpenseDTOForLoggedInUserThenUpdateExpenseSuccessfully() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Expense expense = objectMapper.convertValue(expenseDTO, Expense.class);
        Category category = new Category();
        category.setId(ID_VALUE);
        category.setName(expenseDTO.getCategoryDTO().getName());
        expense.setCategory(category);

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, expense.getId()))
                .thenReturn(Optional.of(expense));
        when(categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), mockUser)).thenReturn(Optional.of(category));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseDTO result = expenseService.updateExpenseDetails(expense.getId(), expenseDTO);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(expense.getId(), result.getId()),
                () -> assertEquals(expense.getName(), result.getName()),
                () -> assertEquals(expense.getDescription(), result.getDescription()),
                () -> assertEquals(expense.getAmount(), result.getAmount()),
                () -> assertEquals(expense.getDate(), result.getDate()),
                () -> assertEquals(expense.getNotes(), result.getNotes()),
                () -> assertEquals(expense.getId(), result.getCategoryDTO().getId()),
                () -> assertEquals(category.getName(), result.getCategoryDTO().getName())
        );
    }

    @Test
    void whenValidExpenseNameThenReturnFilteredExpenseList() throws IOException {

        // given
        String validName = "Expense 1";
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> expensesPage = new PageImpl<>(List.of(expense), pageable, 1);

        // when
        when(expenseRepository.findByUserIdAndNameContaining(ID_VALUE, validName, pageable))
                .thenReturn(expensesPage);
        List<ExpenseDTO> result = expenseService.readByName(validName, pageable);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expense.getId(), result.get(0).getId()),
                () -> assertEquals(expense.getName(), result.get(0).getName()),
                () -> assertEquals(expense.getDescription(), result.get(0).getDescription()),
                () -> assertEquals(expense.getAmount(), result.get(0).getAmount()),
                () -> assertEquals(expense.getDate(), result.get(0).getDate()),
                () -> assertEquals(expense.getNotes(), result.get(0).getNotes())
        );
    }

    @Test
    void whenValidDateRangeThenReturnPaginatedListOfExpenses() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Date startDate = expense.getDate();
        Date endDate = new Date(System.currentTimeMillis());
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> pageMock = new PageImpl<>(List.of(expense), pageable, 1);

        // when
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(pageMock);
        List<ExpenseDTO> result = expenseService.readByDate(startDate, endDate, pageable);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expenseDTO.getId(), result.get(0).getId()),
                () -> assertEquals(expenseDTO.getName(), result.get(0).getName()),
                () -> assertEquals(expenseDTO.getDescription(), result.get(0).getDescription()),
                () -> assertEquals(expenseDTO.getAmount(), result.get(0).getAmount()),
                () -> assertEquals(expenseDTO.getDate(), result.get(0).getDate()),
                () -> assertEquals(expenseDTO.getNotes(), result.get(0).getNotes())
        );
    }

    @Test
    void whenCategoryExistsAndHasExpensesThenReturnExpenseList() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        Category category = expense.getCategory();
        String categoryName = expense.getCategory().getName();

        // when
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(category));
        when(expenseRepository.findByCategory(category)).thenReturn(List.of(expense));
        List<ExpenseDTO> result = expenseService.getExpensesByCategoryName(categoryName);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expense.getId(), result.get(0).getId()),
                () -> assertEquals(expense.getName(), result.get(0).getName()),
                () -> assertEquals(expense.getDescription(), result.get(0).getDescription()),
                () -> assertEquals(expense.getAmount(), result.get(0).getAmount()),
                () -> assertEquals(expense.getDate(), result.get(0).getDate()),
                () -> assertEquals(expense.getNotes(), result.get(0).getNotes())
        );
    }

    @Test
    void whenCategoryExistsForLoggedInUserThenReturnExpenses() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        Expense expense = objectMapper.readValue(validMessage, Expense.class);
        Category category = expense.getCategory();
        String categoryName = expense.getCategory().getName();

        // when
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameAndUser(categoryName, mockUser)).thenReturn(Optional.of(category));
        when(expenseRepository.findByUserAndCategory(mockUser, category)).thenReturn(List.of(expense));
        List<ExpenseDTO> result = expenseService.getCategoriesByNameForLoggedInUser(categoryName);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(1, result.size()),
                () -> assertEquals(expense.getId(), result.get(0).getId()),
                () -> assertEquals(expense.getName(), result.get(0).getName()),
                () -> assertEquals(expense.getDescription(), result.get(0).getDescription()),
                () -> assertEquals(expense.getAmount(), result.get(0).getAmount()),
                () -> assertEquals(expense.getDate(), result.get(0).getDate()),
                () -> assertEquals(expense.getNotes(), result.get(0).getNotes())
        );
    }

    @Test
    void whenPageableNullThenThrowIllegalArgumentException() {

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            expenseService.getAllExpenses(null);
        }, "Pageable cannot be null");
    }

    @Test
    void whenExpenseNotFoundForUserThenThrowResourceNotFoundException() {

        // given
        long invalidExpenseId = 999L;

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, invalidExpenseId)).thenReturn(Optional.empty());

        // then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpenseById(invalidExpenseId);
        });
        assertEquals("Expense is not found for id " + invalidExpenseId, thrown.getMessage());
    }

    @Test
    void whenExpenseNotFoundForLoggedInUserThenThrowResourceNotFoundException() {

        // given
        Long invalidExpenseId = 999L;

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, invalidExpenseId)).thenReturn(Optional.empty());
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.deleteExpenseById(invalidExpenseId);
        });

        // then
        assertEquals("Expense is not found for id " + invalidExpenseId, thrown.getMessage());
    }

    @Test
    void whenNoExpensesExistForLoggedInUserThenThrowResourceNotFoundException() {

        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> emptyExpensesPage = Page.empty();

        // when
        when(expenseRepository.findByUserId(ID_VALUE, pageable)).thenReturn(emptyExpensesPage);

        // then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.deleteAllExpensesForUser(pageable);
        });
        assertEquals("No expenses found for user " + ID_VALUE, thrown.getMessage());
    }

    @Test
    void whenCategoryNameIsMissing_thenThrowIllegalArgumentException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        expenseDTO.getCategoryDTO().setName(null);

        // when & then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            expenseService.saveExpanseDetails(expenseDTO);  // Should throw IllegalArgumentException
        });
        assertEquals("Category name must be provided to add an expense.", thrown.getMessage());
    }

    @Test
    void whenExpenseNotFoundForLoggedInUserThenUpdateMethodThrowResourceNotFoundException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, expenseDTO.getId()))
                .thenReturn(Optional.empty());

        // then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.updateExpenseDetails(expenseDTO.getId(), expenseDTO);
        });
        assertEquals("Expense is not found for id " + expenseDTO.getId(), thrown.getMessage());
    }

    @Test
    void whenCategoryNotFoundForLoggedInUserThenUpdateMethodThrowResourceNotFoundException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Expense existingExpense = objectMapper.convertValue(expenseDTO, Expense.class);

        // when
        when(expenseRepository.findByUserIdAndId(ID_VALUE, existingExpense.getId()))
                .thenReturn(Optional.of(existingExpense));
        when(categoryRepository.findByNameAndUser(expenseDTO.getCategoryDTO().getName(), mockUser))
                .thenReturn(Optional.empty());

        // then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.updateExpenseDetails(existingExpense.getId(), expenseDTO);
        });
        assertEquals("Category not found for name: " + expenseDTO.getCategoryDTO().getName(), thrown.getMessage());
    }

    @Test
    void whenInvalidExpenseNameThenReturnEmptyList() {

        // given
        String invalidName = "Nonexistent Expense";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> emptyPage = Page.empty(pageable);

        // when
        when(expenseRepository.findByUserIdAndNameContaining(ID_VALUE, invalidName, pageable))
                .thenReturn(emptyPage);
        List<ExpenseDTO> result = expenseService.readByName(invalidName, pageable);

        // then
        assertAll("Empty list checks",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void whenStartDateIsAfterEndDateThenReturnEmptyList() {

        // given
        Date startDate = Date.valueOf("2025-01-01");
        Date endDate = Date.valueOf("2024-12-31");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Expense> pageMock = Page.empty(pageable);

        // when
        when(expenseRepository.findByUserIdAndDateBetween(eq(1L), eq(startDate), eq(endDate), eq(pageable)))
                .thenReturn(pageMock);  // Return empty page
        List<ExpenseDTO> result = expenseService.readByDate(startDate, endDate, pageable);

        // then
        assertAll("Expense DTO checks",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }

    @Test
    void whenCategoryDoesNotExistThenThrowResourceNotFoundException() {

        // given
        String invalidCategoryName = "Nonexistent Category";

        // when
        when(categoryRepository.findByName(invalidCategoryName)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getExpensesByCategoryName(invalidCategoryName);
        });
    }

    @Test
    void whenCategoryDoesNotExistForLoggedInUserThenThrowResourceNotFoundException() {

        // given
        String invalidCategoryName = "Nonexistent Category";

        // when
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        when(categoryRepository.findByNameAndUser(invalidCategoryName, mockUser)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            expenseService.getCategoriesByNameForLoggedInUser(invalidCategoryName);
        });
    }
}