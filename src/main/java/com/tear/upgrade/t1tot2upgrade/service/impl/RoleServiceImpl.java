package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;
import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.exceptions.ItemAlreadyExistsException;
import com.tear.upgrade.t1tot2upgrade.exceptions.ResourceNotFoundException;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleDTO createRole(RoleDTO roleDTO) {
        if (roleRepository.existsByRoleName(roleDTO.getRoleName())) {
            log.error("Role with name '{}' already exists", roleDTO.getRoleName());
            throw new ItemAlreadyExistsException("Role with " + roleDTO.getRoleName() + " already exists");
        }
        Role role = new Role();
        role.setRoleName(roleDTO.getRoleName());
        Role savedRole = roleRepository.save(role);
        log.info("Role with name '{}' created successfully", savedRole.getRoleName());
        return convertToDTO(savedRole);
    }

    @Override
    public RoleDTO readRole(Long roleId) {
        Role roleById = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Role with ID '{}' not found", roleId);
                    return new ResourceNotFoundException("Role not found for id: " + roleId);
                });

        log.info("Role with ID '{}' found", roleId);
        return convertToDTO(roleById);
    }

    @Override
    public RoleDTO updateRole(Long roleId, RoleDTO roleDTO) {
        RoleDTO currentRole = readRole(roleId);
        Role role = new Role();
        role.setId(currentRole.getId());
        role.setRoleName(roleDTO.getRoleName() != null ? roleDTO.getRoleName() : currentRole.getRoleName());
        log.info("Saving updated role with ID: {}", role.getId());
        return convertToDTO(roleRepository.save(role));
    }

    @Override
    public List<RoleDTO> readAllRoles() {
        List<Role> roles = roleRepository.findAll();
        log.info("Fetched {} roles", roles.size());
        return roles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private RoleDTO convertToDTO(Role role) {
        if (role == null) {
            log.error("Attempted to convert null role to DTO");
            throw new IllegalArgumentException("Role must not be null");
        }
        log.debug("Converting role with ID '{}' to DTO", role.getId());
        return RoleDTO.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .build();
    }
}
