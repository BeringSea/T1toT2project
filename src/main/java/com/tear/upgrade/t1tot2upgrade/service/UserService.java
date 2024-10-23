package com.tear.upgrade.t1tot2upgrade.service;


import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;

public interface UserService {

    User createUser(UserDTO userDTO);

    User readUser();

    User updateUser(User user);

    void deleteUser();

    User getLoggedInUser();

    User updateUserById(Long userId, User user);
}
