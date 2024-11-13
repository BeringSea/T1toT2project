package com.tear.upgrade.t1tot2upgrade.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenTest {

    @InjectMocks
    private JwtToken jwtToken;

    @Mock
    private UserDetails userDetails;

    private String token;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(userDetails.getUsername()).thenReturn("john.doe@example.com");
        token = jwtToken.generateToken(userDetails);
    }

    @Test
    void whenGenerateTokenThenValidTokenGenerated() {

        // given
        String token = jwtToken.generateToken(userDetails);

        // when & then
        assertAll("JWT Token Validation",
                () -> assertNotNull(token),
                () -> {
                    String[] parts = token.split("\\.");
                    assertEquals(3, parts.length, "JWT should have three parts");
                },
                () -> {
                    String header = new String(Base64.getDecoder().decode(token.split("\\.")[0]));
                    assertTrue(header.contains("HS256"), "Header should contain the signing algorithm HS256");
                },
                () -> {
                    String payload = new String(Base64.getDecoder().decode(token.split("\\.")[1]));
                    assertTrue(payload.contains("john.doe@example.com"), "Payload should contain the username (email)");
                }
        );
    }

    @Test
    void whenExtractUsernameThenUsernameExtractedSuccessfully() {

        // when & then
        assertEquals("john.doe@example.com", jwtToken.extractUserName(token));
    }

    @Test
    void whenValidateToken_thenTokenValid() {

        // when & then
        assertTrue(jwtToken.validateToken(token, userDetails));
    }

    @Test
    void whenValidateTokenWithInvalidTokenThenTokenInvalid() {

        // given
        UserDetails invalidUserDetails = mock(UserDetails.class);
        when(invalidUserDetails.getUsername()).thenReturn("invalid.email@example.com");

        // when & then
        assertFalse(jwtToken.validateToken(token, invalidUserDetails), "Token should be invalid for a user with different username/email");
    }
}