package com.tear.upgrade.t1tot2upgrade.service;


import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.UserModel;

public interface UserService {

    User createUser(UserModel userModel);

    User readUser();

    User updateUser(User user);

    void deleteUser();

    User getLoggedInUser();
}
