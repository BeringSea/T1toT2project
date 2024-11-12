package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Profile;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.ProfileRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import com.tear.upgrade.t1tot2upgrade.utils.FileHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileServiceImplTest {

    public static final long ID_VALUE = 1L;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Mock
    private UserService userService;

    @Mock
    private ProfileRepository profileRepository;

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
    void whenUserLoggedInThenGetProfileSuccess() throws IOException {

        // given
        String validMessage = FileHelper.readFromFile("requests/profile/Profile.json");
        Profile profile = objectMapper.readValue(validMessage, Profile.class);

        // when
        when(profileRepository.findByUserId(ID_VALUE)).thenReturn(Optional.of(profile));
        ProfileDTO resultDto = profileService.getProfileForLoggedInUser();

        // then
        assertAll("Profile DTO checks",
                () -> assertNotNull(resultDto),
                () -> assertEquals(profile.getId(), resultDto.getId()),
                () -> assertEquals(profile.getFirstName(), resultDto.getFirstName()),
                () -> assertEquals(profile.getLastName(), resultDto.getLastName()),
                () -> assertEquals(profile.getPhoneNumber(), resultDto.getPhoneNumber()),
                () -> assertEquals(profile.getAddress(), resultDto.getAddress())
        );
    }

    @Test
    void whenUserLoggedInButProfileNotFoundThenThrowException() {

        // when
        when(profileRepository.findByUserId(ID_VALUE)).thenReturn(Optional.empty());

        // then
        assertThrows(ResourceNotFoundException.class, () -> {
            profileService.getProfileForLoggedInUser();
        }, "Profile not found for user " + ID_VALUE);
    }

}