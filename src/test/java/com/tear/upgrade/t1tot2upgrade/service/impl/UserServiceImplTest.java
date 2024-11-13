package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Profile;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    public static final long ID_VALUE = 1L;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder bcryptEncoder;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        User mockUser = mock(User.class);
        mockUser.setId(1L);
        mockUser.setEmail("john.doe@example.com");
        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getEmail()).thenReturn("john.doe@example.com");
        when(userService.getLoggedInUser()).thenReturn(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(mockUser.getEmail(), null);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        objectMapper = new ObjectMapper();
    }

    @Test
    void whenValidUserDTOThenCreateUserSuccessfully() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);
        UserDTO userDTO = objectMapper.readValue(validMessage, UserDTO.class);
        Profile profile = user.getProfile();
        Role defaultRole = user.getRoles().stream()
                .filter(role -> "USER".equals(role.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found for user"));


        // when
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(defaultRole));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        User result = userServiceImpl.createUser(userDTO);

        // then
        assertAll("User creation checks",
                () -> assertNotNull(result),
                () -> assertEquals(user.getEmail(), result.getEmail()),
                () -> assertTrue(user.getRoles().contains(defaultRole)),
                () -> assertNotNull(user.getProfile()),
                () -> assertEquals(profile.getFirstName(), result.getProfile().getFirstName()),
                () -> assertEquals(profile.getLastName(), result.getProfile().getLastName())
        );
    }

    @Test
    void whenValidUserIdThenReadUserSuccessfully() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);
        when(userService.getLoggedInUser()).thenReturn(user);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        // when
        User result = userServiceImpl.readUser();

        // then
        assertAll("User reading checks",
                () -> assertNotNull(result),
                () -> assertEquals(user.getId(), result.getId()),
                () -> assertEquals(user.getUsername(), result.getUsername()),
                () -> assertEquals(user.getEmail(), result.getEmail())
        );
    }

    @Test
    void whenValidUserAuthenticatedThenReturnLoggedInUser() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);
        String expectedEmail = user.getEmail();

        // when
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        User loggedInUser = userServiceImpl.getLoggedInUser();

        // then
        assertAll("User verification",
                () -> assertNotNull(loggedInUser),
                () -> assertEquals(expectedEmail, loggedInUser.getEmail()),
                () -> verify(userRepository, times(1)).findByEmail(expectedEmail)
        );
    }

    @Test
    void whenValidUserIdThenUpdateUserSuccessfully() throws IOException {
        // given
        String validExistingUser = FileHelper.readFromFile("requests/user/User.json");
        String validUpdatedUser = FileHelper.readFromFile("requests/user/UserUpdated.json");
        User existingUser = objectMapper.readValue(validExistingUser, User.class);
        User updatedUser = objectMapper.readValue(validUpdatedUser, User.class);
        Role defaultRole = existingUser.getRoles().stream()
                .filter(role -> "ADMIN".equals(role.getRoleName()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found for user"));


        // when
        when(roleRepository.findByRoleName(anyString())).thenReturn(Optional.of(defaultRole));
        when(userRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(updatedUser);
        User result = userServiceImpl.updateUserById(updatedUser.getId(), updatedUser);

        // then
        assertAll("User update checks",
                () -> assertNotNull(result),
                () -> assertEquals(updatedUser.getEmail(), result.getEmail()),
                () -> verify(userRepository, times(1)).findById(existingUser.getId()),
                () -> verify(userRepository, times(1)).save(existingUser)
        );
    }


    @Test
    void whenEmailAlreadyRegisteredThenThrowItemAlreadyExistsException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        UserDTO userDTO = objectMapper.readValue(validMessage, UserDTO.class);

        // when
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(true);

        // then
        assertThrows(ItemAlreadyExistsException.class, () -> {
            userServiceImpl.createUser(userDTO);
        });
    }

    @Test
    void whenRequiredFieldsAreMissingThenThrowValidationException() throws IOException {

        // given
        String invalidMessage = FileHelper.readFromFile("requests/user/UserInvalid.json");
        UserDTO userDTO = objectMapper.readValue(invalidMessage, UserDTO.class);

        // when
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            userServiceImpl.createUser(userDTO);
        });
    }

    @Test
    void whenUserNotFoundThenThrowResourceNotFoundException() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/user/User.json");
        User user = objectMapper.readValue(validMessage, User.class);

        // when
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userServiceImpl.readUser();
        });

        // then
        assertEquals("User with not found for email: " + user.getEmail(), exception.getMessage());
    }

    @Test
    void whenUserNotFoundThenThrowUsernameNotFoundException() {

        // given
        String invalidEmail = "john.doe@example.com";

        // when
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userServiceImpl.getLoggedInUser();
        });

        // then
        assertEquals("User with not found for email: " + invalidEmail, exception.getMessage());
    }

    @Test
    void whenUserNotFoundThenThrowException() throws IOException {

        // given
        String validUpdatedUser = FileHelper.readFromFile("requests/user/UserUpdated.json");
        User updatedUser = objectMapper.readValue(validUpdatedUser, User.class);

        // when
        when(userRepository.findById(ID_VALUE)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            userServiceImpl.updateUserById(ID_VALUE, updatedUser);
        });
    }

}