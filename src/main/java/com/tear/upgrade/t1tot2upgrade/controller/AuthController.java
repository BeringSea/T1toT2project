package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.AuthModel;
import com.tear.upgrade.t1tot2upgrade.entity.model.JwtResponseModel;
import com.tear.upgrade.t1tot2upgrade.dto.UserDTO;
import com.tear.upgrade.t1tot2upgrade.security.CustomUserDetailService;
import com.tear.upgrade.t1tot2upgrade.service.JwtToken;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailService userDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtToken jwtToken;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseModel> login(@RequestBody AuthModel authModel) throws Exception {
        authenticate(authModel.getEmail(), authModel.getPassword());
        final UserDetails userDetails = userDetailService.loadUserByUsername(authModel.getEmail());
        final String token = jwtToken.generateToken(userDetails);
        log.info("Generated token for email: {}", authModel.getEmail());
        return new ResponseEntity<>(new JwtResponseModel(token), HttpStatus.OK);
    }


    @PostMapping("/register")
    public ResponseEntity<User> save(@Valid @RequestBody UserDTO userDTO) {
        log.info("Registering new user with email: {}", userDTO.getEmail());
        return new ResponseEntity<>(userService.createUser(userDTO), HttpStatus.CREATED);
    }

    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            log.info("Authentication successful for email: {}", email);
        } catch (DisabledException e) {
            log.error("User with email {} is disabled", email);
            throw new Exception("User disabled");
        } catch (BadCredentialsException e) {
            log.error("Invalid credentials for email: {}", email);
            throw new Exception("Bad credentials");
        }
    }
}
