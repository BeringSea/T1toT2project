package com.tear.upgrade.t1tot2upgrade.entity.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserModel {

    @NotBlank(message = "Name should not be empty")
    private String username;

    @NotNull(message = "Email should not be empty")
    @Email(message = "Enter valid email")
    private String email;

    @NotNull(message = "Password should not be empty")
    @Size(min = 5, message = "Password should be at least 5 characters long")
    private String password;
}
