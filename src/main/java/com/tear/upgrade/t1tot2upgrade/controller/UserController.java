package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import com.tear.upgrade.t1tot2upgrade.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user")
    public ResponseEntity<User> readUser() {
        log.info("Received request to retrieve user details.");
        return new ResponseEntity<>(userService.readUser(), HttpStatus.OK);
    }

    @PutMapping("/user")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        log.info("Received request to update user details: {}", user.getEmail());
        return new ResponseEntity<>(userService.updateUser(user), HttpStatus.OK);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<User> updateUserById(@PathVariable Long id, @RequestBody User user) {
        log.info("Received request to update user with ID: {}", id);
        return new ResponseEntity<>(userService.updateUserById(id, user), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteUser() {
        log.info("Received request to delete user.");
        userService.deleteUser();
        log.info("Successfully deleted user.");
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
