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

import static org.junit.jupiter.api.Assertions.*;
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
        String validMessagesArray = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessagesArray, User.class);

        // when
        when(userService.readUser()).thenReturn(user);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessagesArray)
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
}