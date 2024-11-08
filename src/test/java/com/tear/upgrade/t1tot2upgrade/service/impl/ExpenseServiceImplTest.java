package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.ExpenseDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Expense;
import com.tear.upgrade.t1tot2upgrade.entity.User;
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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpenseServiceImplTest {

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Mock
    private UserService userService;

    @Mock
    private User loggedInUser;

    @Mock
    private ExpenseRepository expenseRepository;

    private ObjectMapper objectMapper;

    private ExpenseDTO expenseDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        objectMapper = new ObjectMapper();
        expenseDTO = mock(ExpenseDTO.class);
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
                // Ensure result is not null and that there are exactly 2 expenses in the content
                () -> assertNotNull(result),
                () -> assertEquals(2, result.getContent().size()),
                () -> {
                    ExpenseDTO expenseDTO1 = result.getContent().get(0);
                    assertThat(expenseDTO1).isNotNull();
                    assertThat(expenseDTO1.getId()).isEqualTo(1L);
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
}