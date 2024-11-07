package com.tear.upgrade.t1tot2upgrade.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
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
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private JwtToken jwtToken;

    @MockBean
    private CustomUserDetailService customUserDetailService;


    private ObjectMapper objectMapper;

    private String validMessage;

    private String invalidMessage;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenGetRoleSuccess() throws Exception {
        // given
        String validMessagesArray = FileHelper.readFromFile("requests/role/RoleArray.json");
        List<RoleDTO> roles = Arrays.asList(objectMapper.readValue(validMessagesArray, RoleDTO[].class));

        // when
        when(roleService.readAllRoles()).thenReturn(roles);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(validMessagesArray)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$[1].roleName").value("USER"))
                .andExpect(jsonPath("$[2].roleName").value("MODERATOR"));
        ;
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenSaveRoleSuccess() throws Exception {

        // given
        String validMessagesArray = FileHelper.readFromFile("requests/role/Role.json");
        RoleDTO roleDTO = objectMapper.readValue(validMessagesArray, RoleDTO.class);

        // when
        when(roleService.createRole(any(RoleDTO.class))).thenReturn(roleDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    @WithMockUser
    void whenUserLoggedInThenUpdateRoleSuccess() throws Exception {

        // given
        String validMessagesArray = FileHelper.readFromFile("requests/role/Role.json");
        RoleDTO updatedRoleDTO = objectMapper.readValue(validMessagesArray, RoleDTO.class);

        Long roleId = 1L;
        RoleDTO currentRoleDTO = RoleDTO.builder()
                .id(roleId)
                .roleName("ADMIN")
                .build();

        // when
        when(roleService.readRole(roleId)).thenReturn(currentRoleDTO);
        when(roleService.updateRole(eq(roleId), any(RoleDTO.class))).thenReturn(updatedRoleDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/roles/{roleId}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRoleDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    @WithMockUser
    void whenRoleExists_thenReturnRoleDTO() throws Exception {

        // given
        String validMessagesArray = FileHelper.readFromFile("requests/role/Role.json");
        RoleDTO roleDTO = objectMapper.readValue(validMessagesArray, RoleDTO.class);
        Long roleId = roleDTO.getId();

        // when
        when(roleService.readRole(roleId)).thenReturn(roleDTO);

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/roles/{id}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(roleId))
                .andExpect(jsonPath("$.roleName").value("ADMIN"));
    }

    @Test
    @WithMockUser
    void whenInvalidRoleInsertThenThrowIllegalArgumentExceptionException() throws Exception {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/role/RoleInvalid.json");
        RoleDTO invalidRoleDTO = objectMapper.readValue(invalidMessage, RoleDTO.class);

        // when
        when(roleService.createRole(any(RoleDTO.class))).thenThrow(new IllegalArgumentException("Role name must not be empty"));

        // when
        mockMvc.perform(MockMvcRequestBuilders.post("/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidRoleDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role name must not be empty"));
    }

    @Test
    @WithMockUser
    void whenInvalidRoleUpdateThenThrowIllegalArgumentExceptionException() throws Exception {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/role/RoleInvalid.json");
        RoleDTO invalidRoleDTO = objectMapper.readValue(invalidMessage, RoleDTO.class);
        Long roleId = invalidRoleDTO.getId();

        // when
        when(roleService.updateRole(eq(roleId), any(RoleDTO.class)))
                .thenThrow(new IllegalArgumentException("Role name must not be empty"));

        // then
        mockMvc.perform(MockMvcRequestBuilders.put("/roles/{roleId}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRoleDTO))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Role name must not be empty"));
    }

    @Test
    @WithMockUser
    void whenRoleNotFoundThenThrowResourceNotFoundException() throws Exception {

        // given
        Long roleId = 999L;

        // when
        when(roleService.readRole(roleId)).thenThrow(new ResourceNotFoundException("Role not found for id: " + roleId));

        // then
        mockMvc.perform(MockMvcRequestBuilders.get("/roles/{id}", roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role not found for id: 999"));
    }

}