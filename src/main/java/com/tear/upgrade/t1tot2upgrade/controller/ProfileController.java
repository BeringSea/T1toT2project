package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;
import com.tear.upgrade.t1tot2upgrade.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping("/profile")
    public ProfileDTO getProfileForLoggedInUser() {
        log.info("Received request to get profile details for the logged-in user.");
        return profileService.getProfileForLoggedInUser();
    }
}
