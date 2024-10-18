package com.tear.upgrade.t1tot2upgrade.service;


import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.UserModel;

public interface UserService {

    User createUser(UserModel userModel);

    User readUser(Long userId);

    User updateUser(User user, Long userId);

    void deleteUser(Long userId);

    User getLoggedInUser();
}
