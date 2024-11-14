package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Profile;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
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
            log.error("User already registered with email: {}", userDTO.getEmail());
            throw new ItemAlreadyExistsException("User is already registered with email " + userDTO.getEmail());
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user, "roleNames", "profile");

        user.setPassword(bcryptEncoder.encode(userDTO.getPassword()));
        log.debug("Password encoded for user with email: {}", userDTO.getEmail());

        Role defaultRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Default role 'ROLE_USER' not found");
                    return new ResourceNotFoundException("Default role not found");
                });

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);
        defaultRole.getUsers().add(user);

        if (userDTO.getProfile() != null) {
            Profile profile = new Profile();
            BeanUtils.copyProperties(userDTO.getProfile(), profile);
            profile.setUser(user);
            user.setProfile(profile);
            log.debug("Profile set for user with email: {}", userDTO.getEmail());
        }

        log.info("Saving new user with email: {}", userDTO.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User readUser() {
        Long userId = getLoggedInUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for ID: {}", userId);
                    return new ResourceNotFoundException("User not found for the id:" + userId);
                });

        log.info("User with ID: {} found", userId);
        return user;
    }

    @Transactional
    @Override
    public User updateUser(User user) {
        User currentUser = readUser();
        populateUserFields(user, currentUser);
        log.info("Updating user with ID: {}", currentUser.getId());
        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public void deleteUser() {
        User currentUser = readUser();
        currentUser.getRoles().clear();
        userRepository.delete(currentUser);
        log.info("User with ID: {} deleted successfully", getLoggedInUser().getId());
    }

    @Override
    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for email: {}", email);
                    return new UsernameNotFoundException("User with not found for email: " + email);
                });
    }

    @Override
    public User updateUserById(Long userId, User user) {
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for ID: {}. ResourceNotFoundException is thrown", userId);
                    return new ResourceNotFoundException("User not found for the id:" + userId);
                });
        populateUserFields(user, currentUser);
        log.info("Updating user by ID: {}", currentUser.getId());
        return userRepository.save(currentUser);
    }

    private Set<Role> getRolesFromNames(Collection<String> roleNames) {
        return roleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> {
                            log.error("Role '{}' not found", roleName);
                            return new ResourceNotFoundException("Role " + roleName + " not found");
                        }))
                .collect(Collectors.toSet());
    }

    private void populateUserFields(User user, User currentUser) {
        if (user.getUsername() != null && user.getUsername().isEmpty()) {
            log.error("Attempted to update with empty username");
            throw new IllegalArgumentException("Username cannot be empty");
        }
        currentUser.setUsername(user.getUsername() != null ? user.getUsername() : currentUser.getUsername());
        if (user.getEmail() != null && !user.getEmail().matches("^[^@]+@[^@]+$")) {
            log.error("Invalid email format: {}", user.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }
        currentUser.setEmail(user.getEmail() != null ? user.getEmail() : currentUser.getEmail());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            log.debug("Updating password for user with email: {}", currentUser.getEmail());
            currentUser.setPassword(bcryptEncoder.encode(user.getPassword()));
        }
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            Set<Role> roles = getRolesFromNames(user.getRoles().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toSet()));

            currentUser.getRoles().clear();
            currentUser.getRoles().addAll(roles);
            log.debug("Roles updated for user with ID: {}", currentUser.getId());
        } else {
            log.error("No roles provided for user with ID: {}", currentUser.getId());
            throw new IllegalArgumentException("At least one role is required");
        }

        if (currentUser.getProfile() != null) {
            Profile existingProfile = currentUser.getProfile();
            existingProfile.setFirstName(user.getProfile().getFirstName() != null ? user.getProfile().getFirstName() : existingProfile.getFirstName());
            existingProfile.setLastName(user.getProfile().getLastName() != null ? user.getProfile().getLastName() : existingProfile.getLastName());
            existingProfile.setPhoneNumber(user.getProfile().getPhoneNumber() != null ? user.getProfile().getPhoneNumber() : existingProfile.getPhoneNumber());
            existingProfile.setAddress(user.getProfile().getAddress() != null ? user.getProfile().getAddress() : existingProfile.getAddress());
            log.debug("Profile updated for user with ID: {}", currentUser.getId());
        } else {
            log.error("Profile information is required for user with ID: {}", currentUser.getId());
            throw new IllegalArgumentException("Profile information is required");
        }
    }
}
