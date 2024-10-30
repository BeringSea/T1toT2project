package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.AuthModel;
import com.tear.upgrade.t1tot2upgrade.entity.model.JwtResponseModel;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailService userDetailService;

    @Mock
    private UserService userService;

    @Mock
    private JwtToken jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        try (var ignored = MockitoAnnotations.openMocks(this)) {
            MockMvcBuilders.standaloneSetup(authController).build();
        }
    }

    @Test
    void whenEmailAndPasswordPassedUserLoggedInWithSuccess() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setEmail("test@example.com");
        authModel.setPassword("password");

        UserDetails userDetails = mock(UserDetails.class);
        String token = "mockJwtToken";

        when(userDetailService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(jwtToken.generateToken(any(UserDetails.class))).thenReturn(token);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        ResponseEntity<JwtResponseModel> response = authController.login(authModel);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(token, Objects.requireNonNull(response.getBody()).jwtToken());
    }

    @ParameterizedTest
    @MethodSource("loginTestData")
    void whenInvalidCredentialsThenThrowCorrespondingException(String email, String password, Exception thrownException, String expectedMessage) {
        AuthModel authModel = new AuthModel();
        authModel.setEmail(email);
        authModel.setPassword(password);

        doThrow(thrownException).when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        Exception exception = assertThrows(Exception.class, () -> authController.login(authModel));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void whenEmailAndPasswordPassedUserCreatedWithSuccess() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");

        User user = new User();
        user.setEmail(userDTO.getEmail());

        when(userService.createUser(any(UserDTO.class))).thenReturn(user);

        ResponseEntity<User> response = authController.save(userDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user.getEmail(), Objects.requireNonNull(response.getBody()).getEmail());
    }


    @Test
    void whenEmailAlreadyExistsThenRuntimeExceptionIsThrown() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("test@example.com");
        userDTO.setPassword("password");

        doThrow(new RuntimeException("Email already exists")).when(userService).createUser(any(UserDTO.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authController.save(userDTO));

        assertEquals("Email already exists", exception.getMessage());
    }

    static Stream<Object[]> loginTestData() {
        return Stream.of(
                new Object[]{"test@example.com", "wrongPassword", new BadCredentialsException("Bad credentials"), "Bad credentials"},
                new Object[]{"test@example.com", "password", new DisabledException("User disabled"), "User disabled"}
        );
    }
}