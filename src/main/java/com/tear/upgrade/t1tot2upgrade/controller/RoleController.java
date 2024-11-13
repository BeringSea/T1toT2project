package com.tear.upgrade.t1tot2upgrade.controller;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/roles")
    private ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) {
        log.info("Received request to create a new role: {}", roleDTO.getRoleName());
        return new ResponseEntity<>(roleService.createRole(roleDTO), HttpStatus.CREATED);
    }

    @GetMapping("/roles")
    private List<RoleDTO> getAllRoles() {
        log.info("Received request to retrieve all roles.");
        return roleService.readAllRoles();
    }

    @GetMapping("roles/{id}")
    private ResponseEntity<RoleDTO> readRole(@PathVariable Long id) {
        log.info("Received request to retrieve role with ID: {}", id);
        return new ResponseEntity<>(roleService.readRole(id), HttpStatus.OK);
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<RoleDTO> updateRole(@PathVariable Long id, @RequestBody RoleDTO roleDTO) {
        log.info("Received request to update role with ID: {}", id);
        return new ResponseEntity<>(roleService.updateRole(id, roleDTO), HttpStatus.OK);
    }
}
