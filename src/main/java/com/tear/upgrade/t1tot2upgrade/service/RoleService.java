package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.entity.Role;

import java.util.List;

public interface RoleService {

    Role createRole(Role role);

    Role readRole(Long id);

    Role updateRole(Long roleId, Role role);

    List<Role> readAllRoles();
}
