package com.tear.upgrade.t1tot2upgrade.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId ;

    @NotBlank(message = "Expense name should not be empty")
    @Size(min = 3, message = "Expense name must be at least 3 characters")
    private String name;

    private String description;

    @NotNull(message = "Expense amount cannot be null")
    private BigDecimal amount;

    @NotNull(message = "Date cannot be null")
    private Date date;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(nullable = false, updatable = false)
    @UpdateTimestamp
    private Timestamp updatedAt;

    private String notes;
}
