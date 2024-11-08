package com.tear.upgrade.t1tot2upgrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.service.ProfileService;
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

import java.io.IOException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileService profileService;

    @MockBean
    private JwtToken jwtToken;

    @MockBean
    private CustomUserDetailService customUserDetailService;

    @MockBean
    private UserService userService;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser()
    void whenUserLoggedInThenGetProfileSuccess() throws Exception {

        // given
        ObjectMapper objectMapper = new ObjectMapper();
        String validMessage = FileHelper.readFromFile("requests/profile/Profile.json");
        ProfileDTO profileDTO = objectMapper.readValue(validMessage, ProfileDTO.class);

        // when
        when(profileService.getProfileForLoggedInUser()).thenReturn(profileDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessage)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.address").value("1234 Elm Street, Springfield, IL"))
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    @WithMockUser()
    void whenProfileNotFoundForLoggedInUserThenThrowResourceNotFoundException() throws Exception {
        // given
        when(profileService.getProfileForLoggedInUser())
                .thenThrow(new ResourceNotFoundException("Profile not found for user"));

        // when and then
        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Profile not found for user")));
    }
}