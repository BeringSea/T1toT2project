package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/roles")
    private ResponseEntity<Role> save(@Valid @RequestBody Role role){
        return new ResponseEntity<>(roleService.createRole(role), HttpStatus.OK);
    }
}
