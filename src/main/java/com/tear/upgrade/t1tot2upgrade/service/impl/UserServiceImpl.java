package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.UserModel;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(UserModel userModel) {
        if(userRepository.existsByEmail(userModel.getEmail())){
            throw new ItemAlreadyExistsException("User is already registered with email " + userModel.getEmail());
        }
        User user = new User();
        BeanUtils.copyProperties(userModel, user);
        return userRepository.save(user);
    }

    @Override
    public User readUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found for the id:" + userId));
    }

    @Override
    public User updateUser(User user, Long userId) {
        User currentUser = readUser(userId);
        currentUser.setUsername(user.getUsername() != null ? user.getUsername() : currentUser.getUsername());
        currentUser.setEmail(user.getEmail() != null ? user.getEmail() : currentUser.getEmail());
        currentUser.setPassword(user.getPassword() != null ? user.getPassword() : currentUser.getPassword());
        return userRepository.save(currentUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User currentUser = readUser(userId);
        userRepository.delete(currentUser);
    }
}
