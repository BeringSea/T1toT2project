package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/roles")
    private ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        return new ResponseEntity<>(roleService.createRole(role), HttpStatus.CREATED);
    }

    @GetMapping("/roles")
    private List<Role> getAllRoles(){
        return roleService.readAllRoles();
    }

    @GetMapping("roles/{id}")
    private ResponseEntity<Role> readRole(@PathVariable Long id) {
        return new ResponseEntity<>(roleService.readRole(id), HttpStatus.OK);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        return new ResponseEntity<>(roleService.updateRole(id, role), HttpStatus.OK);
    }
}
