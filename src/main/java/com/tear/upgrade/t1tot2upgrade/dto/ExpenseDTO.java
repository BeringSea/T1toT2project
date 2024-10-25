package com.tear.upgrade.t1tot2upgrade.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;

    @NotBlank(message = "Expense name should not be empty")
    @Size(min = 3, max = 100, message = "Expense name must be between 3 and 100 characters")
    private String name;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @NotNull(message = "Expense amount cannot be null")
    @DecimalMin(value = "0.01", message = "Expense amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    private Date date;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private CategoryDTO categoryDTO;
}
