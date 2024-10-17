package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new ItemAlreadyExistsException("Role with " + role.getRoleName() + " already exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public Role readRole(Long roleId) {
        return roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found for id: " + roleId));
    }

    @Override
    public Role updateRole(Long roleId, Role role) {
        Role currentRole = readRole(roleId);
        currentRole.setRoleName(role.getRoleName() != null ? role.getRoleName() : currentRole.getRoleName());
        return roleRepository.save(currentRole);
    }

    @Override
    public List<Role> readAllRoles() {
        return roleRepository.findAll();
    }
}
