package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;

public interface UserService {

    /**
     * Creates a new user
     *
     * @param userDTO the data transfer object containing user details
     * @return the created {@link User} entity
     */
    User createUser(UserDTO userDTO);

    /**
     * Retrieves the details of the currently logged-in user.
     *
     * @return the {@link User} entity representing the logged-in user
     */
    User readUser();

    /**
     * Updates the details of an existing user.
     *
     * @param user the {@link User} entity containing updated information
     * @return the updated {@link User} entity
     */
    User updateUser(User user);

    /**
     * Deletes the currently logged-in user.
     */
    void deleteUser();

    /**
     * Retrieves the currently logged-in user.
     *
     * @return the {@link User} entity representing the logged-in user
     */
    User getLoggedInUser();

    /**
     * Updates the details of a user by their identifier.
     *
     * @param userId the identifier of the user to update
     * @param user the {@link User} entity containing updated information
     * @return the updated {@link User} entity
     */
    User updateUserById(Long userId, User user);
}
