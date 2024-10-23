package com.tear.upgrade.t1tot2upgrade.security;

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

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

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
        return new UserDTO(
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet())
        );
    }
}
