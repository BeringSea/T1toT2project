package com.tear.upgrade.t1tot2upgrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.CategoryService;
import com.tear.upgrade.t1tot2upgrade.service.ExpenseService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtToken jwtToken;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @MockBean
    private CategoryService categoryService;

    private ObjectMapper objectMapper;

    private String validMessage;

    private String invalidMessage;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        validMessage = FileHelper.readFromFile("requests/expense/Expense.json");
        invalidMessage = FileHelper.readFromFile("requests/expense/ExpenseInvalid.json");
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenGetAllExpensesSuccess() throws Exception {

        String validMessagesArray = FileHelper.readFromFile("requests/expense/ExpenseArray.json");

        List<ExpenseDTO> expenses = Arrays.asList(objectMapper.readValue(validMessagesArray, ExpenseDTO[].class));

        when(expenseService.getAllExpenses(any(Pageable.class))).thenReturn(new PageImpl<>(expenses));

        mockMvc.perform(MockMvcRequestBuilders.get("/expenses")
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Expense 1"))
                .andExpect(jsonPath("$[0].description").value("Description for expense 1"))
                .andExpect(jsonPath("$[0].amount").value(100.00))
                .andExpect(jsonPath("$[0].notes").value("Notes for expense 1"))
                .andExpect(jsonPath("$[0].categoryDTO.name").value("Category 1"))
                .andExpect(jsonPath("$[0].categoryDTO.description").value("Description for category 1"))
                .andExpect(jsonPath("$[1].name").value("Expense 2"))
                .andExpect(jsonPath("$[1].description").value("Description for expense 2"))
                .andExpect(jsonPath("$[1].amount").value(200.50))
                .andExpect(jsonPath("$[1].notes").value("Notes for expense 2"))
                .andExpect(jsonPath("$[1].categoryDTO.name").value("Category 2"))
                .andExpect(jsonPath("$[1].categoryDTO.description").value("Description for category 2"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenGetExpenseByIdSuccess() throws Exception {

        // given
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Long validId = expenseDTO.getId();

        // when
        when(expenseService.getExpenseById(validId)).thenReturn(expenseDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/{id}", validId)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(validId))
                .andExpect(jsonPath("$.name").value("Expense 1"))
                .andExpect(jsonPath("$.description").value("Description for expense 1"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.notes").value("Notes for expense 1"))
                .andExpect(jsonPath("$.categoryDTO.name").value("Category 1"))
                .andExpect(jsonPath("$.categoryDTO.description").value("Description for category 1"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenDeleteExpenseByIdSuccess() throws Exception {
        // given
        Long validExpenseId = 1L;

        // when
        doNothing().when(expenseService).deleteExpenseById(validExpenseId);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/expenses/{id}", validExpenseId)
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(expenseService, times(1)).deleteExpenseById(validExpenseId);
    }

    @Test
    @WithMockUser
    void testDeleteAllExpensesForUser() throws Exception {
        // when
        doNothing().when(expenseService).deleteAllExpensesForUser(any(Pageable.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/expenses")
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(expenseService, times(1)).deleteAllExpensesForUser(any(Pageable.class));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenSaveExpenseSuccess() throws Exception {

        // given
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);

        // when
        when(expenseService.saveExpanseDetails(any(ExpenseDTO.class))).thenReturn(expenseDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Expense 1"))
                .andExpect(jsonPath("$.description").value("Description for expense 1"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.notes").value("Notes for expense 1"))
                .andExpect(jsonPath("$.categoryDTO.name").value("Category 1"))
                .andExpect(jsonPath("$.categoryDTO.description").value("Description for category 1"));
    }

    @Test
    @WithMockUser
    public void whenUserLoggedInThenUpdateExpenseSuccess() throws Exception {
        // given
        String updatedMessage = FileHelper.readFromFile("requests/expense/ExpenseUpdated.json");
        ExpenseDTO expenseDTO = objectMapper.readValue(updatedMessage, ExpenseDTO.class);
        Long expenseId = expenseDTO.getId();

        // when
        when(expenseService.updateExpenseDetails(eq(expenseId), any(ExpenseDTO.class))).thenReturn(expenseDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedMessage)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.name").value("Updated Expense 1"))
                .andExpect(jsonPath("$.description").value("Updated description for expense 1"))
                .andExpect(jsonPath("$.amount").value(150.0))
                .andExpect(jsonPath("$.notes").value("Updated notes for expense 1"))
                .andExpect(jsonPath("$.categoryDTO.name").value("Updated Category 1"))
                .andExpect(jsonPath("$.categoryDTO.description").value("Updated description for category 1"));
    }

    @Test
    @WithMockUser
    public void whenUserLoggedInThenUpdateExpenseDetailsWithCategorySuccess() throws Exception {

        // given
        ExpenseDTO expenseDTO = objectMapper.readValue(validMessage, ExpenseDTO.class);
        Long expenseId = expenseDTO.getId();

        // when
        when(expenseService.updateExpenseDetails(eq(expenseId), any(ExpenseDTO.class))).thenReturn(expenseDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMessage)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Expense 1"))
                .andExpect(jsonPath("$.description").value("Description for expense 1"))
                .andExpect(jsonPath("$.amount").value(100.0))
                .andExpect(jsonPath("$.notes").value("Notes for expense 1"))
                .andExpect(jsonPath("$.categoryDTO.id").value(1))
                .andExpect(jsonPath("$.categoryDTO.name").value("Category 1"))
                .andExpect(jsonPath("$.categoryDTO.description").value("Description for category 1"));
    }


    @ParameterizedTest
    @MethodSource("categoryProvider")
    @WithMockUser
    public void whenUserLoggedInThenGetExpensesByCategorySuccess(String categoryName, List<ExpenseDTO> expenses) throws Exception {

        // when
        when(expenseService.getExpensesByCategoryName(eq(categoryName))).thenReturn(expenses);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/category/{categoryName}", categoryName)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expenses.size()));

        IntStream.range(0, expenses.size()).forEach(i -> {
            ExpenseDTO expense = expenses.get(i);
            try {
                mockMvc.perform(MockMvcRequestBuilders.get("/expenses/category/{categoryName}", categoryName)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$[" + i + "].name").value(expense.getName()))
                        .andExpect(jsonPath("$[" + i + "].description").value(expense.getDescription()))
                        .andExpect(jsonPath("$[" + i + "].amount").value(expense.getAmount()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    @ParameterizedTest
    @MethodSource("categoryProvider")
    @WithMockUser
    public void whenUserLoggedInThenFindByCategoryNameSuccess(String categoryName, List<ExpenseDTO> expenses) throws Exception {

        // when
        when(expenseService.getCategoriesByNameForLoggedInUser(eq(categoryName))).thenReturn(expenses);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/user/category/{categoryName}", categoryName)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expenses.size()));

        IntStream.range(0, expenses.size()).forEach(i -> {
            ExpenseDTO expense = expenses.get(i);
            try {
                mockMvc.perform(MockMvcRequestBuilders.get("/expenses/user/category/{categoryName}", categoryName)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$[" + i + "].name").value(expense.getName()))
                        .andExpect(jsonPath("$[" + i + "].description").value(expense.getDescription()))
                        .andExpect(jsonPath("$[" + i + "].amount").value(expense.getAmount()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("keywordProvider")
    @WithMockUser
    public void whenUserLoggedInThenGetAllExpensesByKeywordSuccess(String keyword, List<ExpenseDTO> expenses) throws Exception {

        // when
        when(expenseService.readByName(eq(keyword), any(Pageable.class))).thenReturn(expenses);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/name")
                        .param("keyword", keyword)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expenses.size()));

        IntStream.range(0, expenses.size()).forEach(i -> {
            ExpenseDTO expense = expenses.get(i);
            try {
                mockMvc.perform(MockMvcRequestBuilders.get("/expenses/name")
                                .param("keyword", keyword)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$[" + i + "].name").value(expense.getName()))
                        .andExpect(jsonPath("$[" + i + "].description").value(expense.getDescription()))
                        .andExpect(jsonPath("$[" + i + "].amount").value(expense.getAmount()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("dateProvider")
    @WithMockUser
    public void whenUserLoggedInThenGetAllExpensesByDateSuccess(String startDate, String endDate, List<ExpenseDTO> expenses) throws Exception {

        // when
        when(expenseService.readByDate(any(Date.class), any(Date.class), any(Pageable.class))).thenReturn(expenses);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/date")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(expenses.size()));

        IntStream.range(0, expenses.size()).forEach(i -> {
            ExpenseDTO expense = expenses.get(i);
            try {
                mockMvc.perform(MockMvcRequestBuilders.get("/expenses/date")
                                .param("startDate", startDate)
                                .param("endDate", endDate)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$[" + i + "].name").value(expense.getName()))
                        .andExpect(jsonPath("$[" + i + "].description").value(expense.getDescription()))
                        .andExpect(jsonPath("$[" + i + "].amount").value(expense.getAmount()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("invalidDateProvider")
    @WithMockUser
    public void whenGetAllExpensesByInvalidDateThenStatusBadRequest(String startDate, String endDate) throws Exception {
        // when & then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/date")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void whenNoExpensesFoundForLoggedInUserThenThrowResourceNotFoundException() throws Exception {
        // when
        when(expenseService.getAllExpenses(any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("No expenses found for the user"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No expenses found for the user")));
    }

    @Test
    @WithMockUser
    void whenExpenseNotFoundThenThrowResourceNotFoundException() throws Exception {

        // given
        Long invalidExpenseId = 99L;

        // when
        when(expenseService.getExpenseById(invalidExpenseId))
                .thenThrow(new ResourceNotFoundException("Expense is not found for id " + invalidExpenseId));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/{id}", invalidExpenseId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Expense is not found for id " + invalidExpenseId)));
    }

    @Test
    @WithMockUser
    void whenExpenseNotFoundForLoggedInUserThenThrowResourceNotFoundException() throws Exception {

        // given
        Long invalidExpenseId = 99L;

        // when
        doThrow(new ResourceNotFoundException("Expense is not found for id " + invalidExpenseId))
                .when(expenseService).deleteExpenseById(invalidExpenseId);

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/expenses/{id}", invalidExpenseId)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Expense is not found for id " + invalidExpenseId)));
    }

    @Test
    @WithMockUser
    void whenNoExpensesFoundForUserThenThrowResourceNotFoundException() throws Exception {

        // when
        doThrow(new ResourceNotFoundException("No expenses found for user 1"))
                .when(expenseService).deleteAllExpensesForUser(any(Pageable.class));

        // then
        mockMvc.perform(MockMvcRequestBuilders.delete("/expenses")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No expenses found for user 1")));
    }

    @Test
    @WithMockUser
    void whenSaveExpenseWithInvalidInputThenStatusBadRequest() throws Exception {

        // given
        invalidMessage = FileHelper.readFromFile("requests/expense/ExpenseInvalid.json");

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMessage)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.messages", hasSize(4)))
                .andExpect(jsonPath("$.messages", hasItem("Expense name should not be empty")))
                .andExpect(jsonPath("$.messages", hasItem("Expense amount cannot be null")))
                .andExpect(jsonPath("$.messages", hasItem("Expense name must be between 3 and 100 characters")))
                .andExpect(jsonPath("$.messages", hasItem("Date cannot be null")));
    }

    @Test
    @WithMockUser
    public void whenUpdateNonExistentExpenseThenStatusNotFound() throws Exception {
        // given
        Long expenseId = 1L;

        // when
        when(expenseService.updateExpenseDetails(eq(expenseId), any(ExpenseDTO.class)))
                .thenThrow(new ResourceNotFoundException("Expense is not found for id " + expenseId));

        // then
        mockMvc.perform(put("/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMessage)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Expense is not found for id " + expenseId)); // Checking the message field
    }

    @Test
    @WithMockUser
    public void whenUpdateExpenseNonExistentCategoryThenStatusNotFound() throws Exception {

        // given
        Long expenseId = 1L;

        // when
        when(expenseService.updateExpenseDetails(eq(expenseId), any(ExpenseDTO.class)))
                .thenThrow(new ResourceNotFoundException("Category not found for name: NonExistentCategory"));

        // then
        mockMvc.perform(put("/expenses/{id}", expenseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMessage)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found for name: NonExistentCategory")); // Checking the message field
    }


    @ParameterizedTest
    @MethodSource("invalidCategoryProvider")
    @WithMockUser
    public void whenGetExpensesByInvalidCategoryThenStatusNotFound(String categoryName) throws Exception {

        // when
        when(expenseService.getExpensesByCategoryName(eq(categoryName))).thenThrow(new ResourceNotFoundException("Category not found: " + categoryName));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/category/{categoryName}", categoryName)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: " + categoryName));
    }

    @ParameterizedTest
    @MethodSource("invalidCategoryProvider")
    @WithMockUser
    public void whenGetCategoriesByInvalidNameForLoggedInUserThenStatusIsNotFound(String categoryName) throws Exception {

        // when
        when(expenseService.getCategoriesByNameForLoggedInUser(eq(categoryName)))
                .thenThrow(new ResourceNotFoundException("Category not found: " + categoryName));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/user/category/{categoryName}", categoryName)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category not found: " + categoryName));
    }

    @ParameterizedTest
    @MethodSource("invalidKeywordProvider")
    @WithMockUser
    public void whenGetAllExpensesByInvalidKeywordThenStatusNotFound(String keyword) throws Exception {

        // when
        when(expenseService.readByName(eq(keyword), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("No expenses found for keyword: " + keyword));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/name")
                        .param("keyword", keyword)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No expenses found for keyword: " + keyword));
    }

    private static Stream<Arguments> categoryProvider() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String validMessagesArray = FileHelper.readFromFile("requests/expense/ExpenseArray.json");
        List<ExpenseDTO> expenses = Arrays.asList(objectMapper.readValue(validMessagesArray, ExpenseDTO[].class));

        return Stream.of(
                Arguments.of("Category 1", expenses.stream()
                        .filter(expense -> expense.getCategoryDTO().getName().equals("Category 1"))
                        .collect(Collectors.toList())),
                Arguments.of("Category 2", expenses.stream()
                        .filter(expense -> expense.getCategoryDTO().getName().equals("Category 2"))
                        .collect(Collectors.toList()))
        );
    }

    private static Stream<Arguments> invalidCategoryProvider() {
        return Stream.of(
                Arguments.of("NonExistentCategory1"),
                Arguments.of("NonExistentCategory2"),
                Arguments.of("AnotherInvalidCategory")
        );
    }

    private static Stream<Arguments> keywordProvider() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String validMessagesArray = FileHelper.readFromFile("requests/expense/ExpenseArray.json");
        List<ExpenseDTO> expenses = Arrays.asList(objectMapper.readValue(validMessagesArray, ExpenseDTO[].class));

        return Stream.of(
                Arguments.of("Expense 1", expenses.stream()
                        .filter(expense -> expense.getName().equals("Expense 1"))
                        .collect(Collectors.toList())),
                Arguments.of("Expense 2", expenses.stream()
                        .filter(expense -> expense.getName().equals("Expense 2"))
                        .collect(Collectors.toList()))
        );
    }

    private static Stream<String> invalidKeywordProvider() {
        return Stream.of("NonExistentExpense", "AnotherInvalidExpense");
    }

    private static Stream<Arguments> dateProvider() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        String validMessagesArray = FileHelper.readFromFile("requests/expense/ExpenseArray.json");

        List<ExpenseDTO> expenses = Arrays.asList(objectMapper.readValue(validMessagesArray, ExpenseDTO[].class));

        return Stream.of(
                Arguments.of("2024-05-01", "2024-11-01", expenses),
                Arguments.of("2024-11-04", "2024-11-11", expenses)
        );
    }

    private static Stream<Arguments> invalidDateProvider() {
        return Stream.of(
                Arguments.of("invalid-date", "2023-12-31"),
                Arguments.of("2023-01-01", "invalid-date"),
                Arguments.of("2023-01-01", ""),
                Arguments.of("", "2023-12-31")
        );
    }
}