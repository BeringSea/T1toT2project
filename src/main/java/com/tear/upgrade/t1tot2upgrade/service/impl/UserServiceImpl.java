package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.UserModel;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder bcryptEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    @Override
    public User createUser(UserModel userModel) {
        if (userRepository.existsByEmail(userModel.getEmail())) {
            throw new ItemAlreadyExistsException("User is already registered with email " + userModel.getEmail());
        }
        User user = new User();
        BeanUtils.copyProperties(userModel, user, "roleNames");
        user.setPassword(bcryptEncoder.encode(user.getPassword()));

        Set<Role> roles = getRolesFromNames(userModel.getRoleNames());
        user.setRoles(roles);
        roles.forEach(role -> role.getUsers().add(user));

        return userRepository.save(user);
    }

    @Override
    public User readUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found for the id:" + userId));
    }

    @Transactional
    @Override
    public User updateUser(User user, Long userId) {
        User currentUser = readUser(userId);
        currentUser.setUsername(user.getUsername() != null ? user.getUsername() : currentUser.getUsername());
        currentUser.setEmail(user.getEmail() != null ? user.getEmail() : currentUser.getEmail());
        currentUser.setPassword(user.getPassword() != null ? bcryptEncoder.encode(user.getPassword()) : currentUser.getPassword());
        if (user.getRoles() != null) {
            Set<Role> roles = getRolesFromNames(user.getRoles().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toSet()));

            currentUser.getRoles().clear();
            currentUser.getRoles().addAll(roles);
        }
        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        User currentUser = readUser(userId);
        currentUser.getRoles().clear();
        userRepository.delete(currentUser);
    }

    private Set<Role> getRolesFromNames(Collection<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found")))
                .collect(Collectors.toSet());
    }
}
