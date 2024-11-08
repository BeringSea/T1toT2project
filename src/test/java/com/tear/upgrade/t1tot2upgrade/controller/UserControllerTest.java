package com.tear.upgrade.t1tot2upgrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtToken jwtToken;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    private ObjectMapper objectMapper;

    private String validMessage;

    private String invalidMessage;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenReadUserSuccess() throws Exception {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);

        // when
        when(userService.readUser()).thenReturn(user);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1].roleName").value("USER"))
                .andExpect(jsonPath("$.profile.firstName").value("John"))
                .andExpect(jsonPath("$.profile.lastName").value("Doe"))
                .andExpect(jsonPath("$.profile.address").value("123 Main Street, Springfield, USA"))
                .andExpect(jsonPath("$.profile.phoneNumber").value("555-1234"));
    }

    @Test
    @WithMockUser
    void whenUserUpdatedThenReturnUpdatedUser() throws Exception {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);

        // when
        when(userService.updateUser(any(User.class))).thenReturn(user);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(user))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1].roleName").value("USER"))
                .andExpect(jsonPath("$.profile.firstName").value("John"))
                .andExpect(jsonPath("$.profile.lastName").value("Doe"))
                .andExpect(jsonPath("$.profile.address").value("123 Main Street, Springfield, USA"))
                .andExpect(jsonPath("$.profile.phoneNumber").value("555-1234"));
    }

    @Test
    @WithMockUser
    void whenInvalidUserUpdatedThenReturnBadRequest() throws Exception {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/user/UserInvalid.json");
        User invalidUser = objectMapper.readValue(invalidMessage, User.class);

        // when
        when(userService.updateUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Username cannot be empty"))
                .thenThrow(new IllegalArgumentException("Invalid email format"))
                .thenThrow(new IllegalArgumentException("Password must be at least 5 characters"))
                .thenThrow(new IllegalArgumentException("First name cannot be empty"))
                .thenThrow(new IllegalArgumentException("Last name cannot be empty"));

        // then
        validateInvalidUserField(invalidUser, "Username cannot be empty");
        validateInvalidUserField(invalidUser, "Invalid email format");
        validateInvalidUserField(invalidUser, "Password must be at least 5 characters");
        validateInvalidUserField(invalidUser, "First name cannot be empty");
        validateInvalidUserField(invalidUser, "Last name cannot be empty");
    }

    @Test
    @WithMockUser
    void whenUserUpdatedByIdThenReturnUpdatedUser() throws Exception {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User validUser = objectMapper.readValue(validMessage, User.class);

        // when
        when(userService.updateUserById(eq(1L), any(User.class))).thenReturn(validUser);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(validUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.profile.firstName").value("John"))
                .andExpect(jsonPath("$.profile.lastName").value("Doe"))
                .andExpect(jsonPath("$.profile.phoneNumber").value("555-1234"))
                .andExpect(jsonPath("$.profile.address").value("123 Main Street, Springfield, USA"));
    }

    @Test
    @WithMockUser
    void whenUserUpdatedByIdWithInvalidIdThenThrowIllegalArgumentException() throws Exception {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/user/UserInvalid.json");
        User invalidUser = objectMapper.readValue(invalidMessage, User.class);
        Long invalidUserId = invalidUser.getId();

        // when
        when(userService.updateUserById(eq(invalidUserId), any(User.class)))
                .thenThrow(new IllegalArgumentException("User with id " + invalidUserId + " does not exist"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/user/" + invalidUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User with id -1 does not exist"));
    }

    private void validateInvalidUserField(User invalidUser, String expectedErrorMessage) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(invalidUser))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(expectedErrorMessage));
    }
}