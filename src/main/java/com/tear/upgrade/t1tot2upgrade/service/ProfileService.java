package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;

import java.util.NoSuchElementException;

public interface ProfileService {

    /**
     * Retrieves the profile information for the currently logged-in user.
     *
     * @return a {@link ProfileDTO} containing the profile details of the logged-in user.
     * @throws NoSuchElementException if the logged-in user does not have an associated profile.
     */
    ProfileDTO getProfileForLoggedInUser();
}
