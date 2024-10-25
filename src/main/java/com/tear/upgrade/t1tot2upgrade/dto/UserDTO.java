package com.tear.upgrade.t1tot2upgrade.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    public UserDTO(String username, String email, Set<String> roleNames, ProfileDTO profile) {
        this.username = username;
        this.email = email;
        this.roleNames = roleNames;
        this.profile = profile;
    }

    @NotBlank(message = "Name should not be empty")
    private String username;

    @NotNull(message = "Email should not be empty")
    @Email(message = "Enter valid email")
    private String email;

    @NotNull(message = "Password should not be empty")
    @Size(min = 5, message = "Password should be at least 5 characters long")
    private String password;

    @NotNull(message = "Roles should not be empty")
    private Set<String> roleNames = new HashSet<>();

    private ProfileDTO profile;
}
