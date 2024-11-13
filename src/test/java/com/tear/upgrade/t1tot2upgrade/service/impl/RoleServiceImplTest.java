package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleServiceImplTest {

    public static final long ID_VALUE = 1L;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Mock
    private UserService userService;

    @Mock
    private RoleRepository roleRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        objectMapper = new ObjectMapper();
    }

    @Test
    void whenUserLoggedInThenSaveRoleSuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/role/Role.json");
        Role role = objectMapper.readValue(validMessage, Role.class);
        RoleDTO roleDTO = objectMapper.readValue(validMessage, RoleDTO.class);

        // when
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        RoleDTO result = roleService.createRole(roleDTO);

        // then
        assertAll("Role DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(role.getId(), result.getId()),
                () -> assertEquals(role.getRoleName(), result.getRoleName())
        );
    }

    @Test
    void whenRoleExistsThenReadRoleSuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/role/Role.json");
        Role role = objectMapper.readValue(validMessage, Role.class);

        // when
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        RoleDTO result = roleService.readRole(role.getId());

        // then
        assertAll("Role DTO checks",
                () -> assertNotNull(result),
                () -> assertEquals(role.getId(), result.getId()),
                () -> assertEquals(role.getRoleName(), result.getRoleName())
        );
    }

    @Test
    void whenRoleExistsThenUpdateRoleSuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/role/Role.json");
        Role role = objectMapper.readValue(validMessage, Role.class);
        RoleDTO roleDTO = objectMapper.readValue(validMessage, RoleDTO.class);

        // when
        when(roleRepository.findById(ID_VALUE)).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        RoleDTO updatedRoleDTO = roleService.updateRole(role.getId(), roleDTO);

        // then
        assertAll("Updated Role DTO checks",
                () -> assertNotNull(updatedRoleDTO),
                () -> assertEquals(role.getId(), updatedRoleDTO.getId()),
                () -> assertEquals(role.getRoleName(), updatedRoleDTO.getRoleName())
        );
    }

    @Test
    void whenRolesExistThenReadAllRolesSuccess() throws IOException {

        // given
        String validMessagesArray = FileHelper.readFromFile("requests/role/RoleArray.json");
        List<Role> roles = Arrays.asList(objectMapper.readValue(validMessagesArray, Role[].class));


        // when
        when(roleRepository.findAll()).thenReturn(roles);
        List<RoleDTO> result = roleService.readAllRoles();

        // then
        assertAll("Read All Roles checks",
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                () -> assertEquals(roles.get(0).getId(), result.get(0).getId()),
                () -> assertEquals(roles.get(0).getRoleName(), result.get(0).getRoleName()),
                () -> assertEquals(roles.get(1).getId(), result.get(1).getId()),
                () -> assertEquals(roles.get(1).getRoleName(), result.get(1).getRoleName())
        );
    }


    @Test
    void whenRoleAlreadyExistsThenThrowItemAlreadyExistsException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/role/Role.json");
        RoleDTO roleDTO = objectMapper.readValue(validMessage, RoleDTO.class);

        // when
        when(roleRepository.existsByRoleName(roleDTO.getRoleName())).thenReturn(true);

        // then
        assertThrows(ItemAlreadyExistsException.class, () -> roleService.createRole(roleDTO));
    }

    @Test
    void whenRoleNotFoundThenThrowResourceNotFoundException() {

        // given
        Long nonExistentRoleId = 999L;

        // when
        when(roleRepository.findById(nonExistentRoleId)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> roleService.readRole(nonExistentRoleId),
                "Role not found for id: " + nonExistentRoleId);
    }

    @Test
    void whenRoleNotFoundForRoleUpdateThenThrowResourceNotFoundException() throws IOException {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/role/RoleInvalid.json");
        RoleDTO roleInvalidDTO = objectMapper.readValue(invalidMessage, RoleDTO.class);
        Long nonExistentRoleId = roleInvalidDTO.getId();

        // when
        when(roleRepository.findById(nonExistentRoleId)).thenThrow(new ResourceNotFoundException("Role not found for id: " + nonExistentRoleId));

        // then
        assertThrows(ResourceNotFoundException.class, () -> roleService.updateRole(nonExistentRoleId, roleInvalidDTO),
                "Role not found for id: " + nonExistentRoleId);
    }

    @Test
    void whenNoRolesExistThenReadAllRolesReturnsEmptyList() {

        // given
        List<Role> roles = List.of();

        // when
        when(roleRepository.findAll()).thenReturn(roles);
        List<RoleDTO> result = roleService.readAllRoles();

        // then
        assertAll("Read All Roles checks for empty list",
                () -> assertNotNull(result),
                () -> assertTrue(result.isEmpty())
        );
    }
}