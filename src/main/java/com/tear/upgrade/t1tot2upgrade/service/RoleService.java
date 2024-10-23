package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    RoleDTO createRole(RoleDTO role);

    RoleDTO readRole(Long id);

    RoleDTO updateRole(Long roleId, RoleDTO role);

    List<RoleDTO> readAllRoles();
}
