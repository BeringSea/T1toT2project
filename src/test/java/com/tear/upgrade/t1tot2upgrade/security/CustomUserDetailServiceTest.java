package com.tear.upgrade.t1tot2upgrade.security;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomUserDetailServiceTest {

    public static final long ID_VALUE = 1L;

    public static final String ROLE_USER = "USER";

    public static final String ROLE_ADMIN = "ADMIN";

    public static final String EMAIL = "john.doe@example.com";

    public static final String PASSWORD = "password123";

    @InjectMocks
    private CustomUserDetailService userDetailService;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User mockUser;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = mock(User.class);
        mockUser.setId(ID_VALUE);
        mockUser.setEmail(EMAIL);
        mockUser.setPassword(PASSWORD);
        Role mockRoleUser = mock(Role.class);
        when(mockRoleUser.getRoleName()).thenReturn(ROLE_USER);
        Role mockRoleAdmin = mock(Role.class);
        when(mockRoleAdmin.getRoleName()).thenReturn(ROLE_ADMIN);
        Set<Role> roles = new HashSet<>();
        roles.add(mockRoleUser);
        roles.add(mockRoleAdmin);
        mockUser.setRoles(roles);
        when(mockUser.getId()).thenReturn(ID_VALUE);
        when(mockUser.getEmail()).thenReturn(EMAIL);
        when(mockUser.getPassword()).thenReturn(PASSWORD);
        when(mockUser.getRoles()).thenReturn(roles);
        when(userService.getLoggedInUser()).thenReturn(mockUser);
    }

    @Test
    void whenValidEmailThenUserDetailsReturned() {

        // given & when
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        UserDetails userDetails = userDetailService.loadUserByUsername(mockUser.getEmail());

        // then
        assertAll("UserDetails assertions",
                () -> assertNotNull(userDetails),
                () -> assertEquals(EMAIL, userDetails.getUsername()),
                () -> assertEquals(PASSWORD, userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")))
        );
    }

    @Test
    void whenInvalidEmailThenUsernameNotFoundException() {

        // given
        String invalidEmail = "invalid.email@example.com";
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailService.loadUserByUsername(invalidEmail);
        });
    }

    @Test
    void whenUserWithNoRolesThenUserDetailsReturned() {

        // given
        User noRolesUserMock = mock(User.class);
        noRolesUserMock.setId(ID_VALUE);
        noRolesUserMock.setEmail(EMAIL);
        noRolesUserMock.setPassword(PASSWORD);
        when(noRolesUserMock.getId()).thenReturn(ID_VALUE);
        when(noRolesUserMock.getEmail()).thenReturn(EMAIL);
        when(noRolesUserMock.getPassword()).thenReturn(PASSWORD);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(noRolesUserMock));

        // when
        UserDetails userDetails = userDetailService.loadUserByUsername(mockUser.getEmail());

        // then
        assertAll("UserDetails assertions for no roles",
                () -> assertNotNull(userDetails),
                () -> assertEquals(EMAIL, userDetails.getUsername()),
                () -> assertEquals(PASSWORD, userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().isEmpty())
        );
    }

    @Test
    void whenUserWithOneRoleThenUserDetailsReturned() {

        // given
        Role oneRoleUserMock = mock(Role.class);
        when(oneRoleUserMock.getRoleName()).thenReturn(ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(oneRoleUserMock);
        mockUser.setRoles(roles);

        // when
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        UserDetails userDetails = userDetailService.loadUserByUsername(mockUser.getEmail());

        // then
        assertAll("UserDetails assertions for one role",
                () -> assertNotNull(userDetails),
                () -> assertEquals(EMAIL, userDetails.getUsername()),
                () -> assertEquals(PASSWORD, userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_USER)))
        );
    }

    @Test
    void whenUserWithMultipleRolesThenUserDetailsReturned() {

        // given & when
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        UserDetails userDetails = userDetailService.loadUserByUsername(mockUser.getEmail());

        // then
        assertAll("UserDetails assertions for multiple roles",
                () -> assertNotNull(userDetails),
                () -> assertEquals(EMAIL, userDetails.getUsername()),
                () -> assertEquals(PASSWORD, userDetails.getPassword()),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_USER))),
                () -> assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(ROLE_ADMIN)))
        );
    }
}