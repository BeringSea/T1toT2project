package com.tear.upgrade.t1tot2upgrade.service;

import com.tear.upgrade.t1tot2upgrade.dto.RoleDTO;

import java.util.List;

public interface RoleService {

    /**
     * Creates a new role.
     *
     * @param role the role data to be created
     * @return the created {@link RoleDTO} instance
     */
    RoleDTO createRole(RoleDTO role);

    /**
     * Retrieves a role by its identifier.
     *
     * @param id the identifier of the role
     * @return the {@link RoleDTO} corresponding to the given id
     */
    RoleDTO readRole(Long id);

    /**
     * Updates an existing role.
     *
     * @param roleId the identifier of the role to update
     * @param role the updated role data
     * @return the updated {@link RoleDTO} instance
     */
    RoleDTO updateRole(Long roleId, RoleDTO role);

    /**
     * Retrieves a list of all roles.
     *
     * @return a list of {@link RoleDTO} representing all roles
     */
    List<RoleDTO> readAllRoles();
}
