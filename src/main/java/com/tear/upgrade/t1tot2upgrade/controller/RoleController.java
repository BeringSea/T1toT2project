package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
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
    private ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        return new ResponseEntity<>(roleService.createRole(roleDTO), HttpStatus.CREATED);
    }

    @GetMapping("/roles")
    private List<RoleDTO> getAllRoles() {
        return roleService.readAllRoles();
    }

    @GetMapping("roles/{id}")
    private ResponseEntity<RoleDTO> readRole(@PathVariable Long id) {
        return new ResponseEntity<>(roleService.readRole(id), HttpStatus.OK);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        return new ResponseEntity<>(roleService.updateRole(id, roleDTO), HttpStatus.OK);
    }
}
