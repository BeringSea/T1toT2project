package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new ItemAlreadyExistsException("User is already registered with email " + userDTO.getEmail());
        }
        User user = new User();
        BeanUtils.copyProperties(userDTO, user, "roleNames");
        user.setPassword(bcryptEncoder.encode(user.getPassword()));

        Set<Role> roles = getRolesFromNames(userDTO.getRoleNames());
        user.setRoles(roles);
        roles.forEach(role -> role.getUsers().add(user));

        return userRepository.save(user);
    }

    @Override
    public User readUser() {
        Long userId = getLoggedInUser().getId();
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found for the id:" + userId));
    }

    @Transactional
    @Override
    public User updateUser(User user) {
        User currentUser = readUser();
        populateUserFields(user, currentUser);
        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public void deleteUser() {
        User currentUser = readUser();
        currentUser.getRoles().clear();
        userRepository.delete(currentUser);
    }

    @Override
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with not found for email: " + email));
    }

    @Override
    public User updateUserById(Long userId, User user) {
        User currentUser = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found for the id:" + userId));
        populateUserFields(user, currentUser);
        return userRepository.save(currentUser);
    }

    private Set<Role> getRolesFromNames(Collection<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Role " + roleName + " not found")))
                .collect(Collectors.toSet());
    }

    private void populateUserFields(User user, User currentUser) {
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
    }
}
