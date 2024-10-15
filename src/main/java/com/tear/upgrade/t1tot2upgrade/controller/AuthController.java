package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.entity.model.UserModel;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(){
        return new ResponseEntity<>("User is logged in", HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> save(@Valid @RequestBody UserModel userModel) {
        return new ResponseEntity<>(userService.createUser(userModel), HttpStatus.CREATED);
    }
}
