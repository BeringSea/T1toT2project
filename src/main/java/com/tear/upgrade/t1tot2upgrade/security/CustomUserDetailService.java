package com.tear.upgrade.t1tot2upgrade.security;

import com.tear.upgrade.t1tot2upgrade.dto.ProfileDTO;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Custom implementation of the {@link UserDetailsService} interface for loading user-specific data.
 * This service retrieves user details from the database and converts them into a format
 * suitable for Spring Security.
 *
 * <p>This service is responsible for:</p>
 * <ul>
 *     <li>Loading a user by their email address</li>
 *     <li>Converting the user entity to a {@link UserDTO}</li>
 *     <li>Mapping roles to {@link GrantedAuthority} objects</li>
 * </ul>
 */
@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads a user by their email address.
     *
     * @param email the email address of the user
     * @return a {@link UserDetails} object containing user information and authorities
     * @throws UsernameNotFoundException if the user with the specified email is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User existingUser = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email: " + email));

        UserDTO userDTO = convertToDTO(existingUser);

        List<GrantedAuthority> authorities = userDTO.getRoleNames().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(userDTO.getEmail(), userDTO.getPassword(), authorities);
    }

    private UserDTO convertToDTO(User user) {
        ProfileDTO profileDTO = null;

        if (user.getProfile() != null) {
            profileDTO = new ProfileDTO();
            profileDTO.setId(user.getProfile().getId());
            profileDTO.setFirstName(user.getProfile().getFirstName());
            profileDTO.setLastName(user.getProfile().getLastName());
            profileDTO.setPhoneNumber(user.getProfile().getPhoneNumber());
            profileDTO.setAddress(user.getProfile().getAddress());
            profileDTO.setUsername(user.getUsername());
        }

        return new UserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()),
                profileDTO
        );
    }
}
