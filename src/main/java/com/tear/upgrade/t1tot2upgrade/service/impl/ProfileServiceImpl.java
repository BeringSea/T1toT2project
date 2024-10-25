package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Profile;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.ProfileRepository;
import com.tear.upgrade.t1tot2upgrade.service.ProfileService;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserService userService;

    @Override
    public ProfileDTO getProfileForLoggedInUser() {
        Long userId = userService.getLoggedInUser().getId();
        return profileRepository.findByUserId(userId)
                .map(this::convertDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for user with ID: " + userId));
    }

    private ProfileDTO convertDTO(Profile profile) {
        return ProfileDTO.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .phoneNumber(profile.getPhoneNumber())
                .address(profile.getAddress())
                .username(profile.getUser().getUsername())
                .build();
    }
}
