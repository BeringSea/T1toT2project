package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            throw new ItemAlreadyExistsException("Role with " + roleDTO.getRoleName() + " already exists");
        }
        Role role = new Role();
        role.setRoleName(roleDTO.getRoleName());
        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Override
    public RoleDTO readRole(Long roleId) {
        Role roleById = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role not found for id: " + roleId));
        return convertToDTO(roleById);
    }

    @Override
    public RoleDTO updateRole(Long roleId, RoleDTO roleDTO) {
        RoleDTO currentRole = readRole(roleId);
        Role role = new Role();
        role.setId(currentRole.getId());
        role.setRoleName(roleDTO.getRoleName() != null ? roleDTO.getRoleName() : currentRole.getRoleName());
        return convertToDTO(roleRepository.save(role));
    }

    @Override
    public List<RoleDTO> readAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO convertToDTO(Role role) {
        return new RoleDTO(
                role.getId(),
                role.getRoleName()
        );
    }
}
