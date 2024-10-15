package com.tear.upgrade.t1tot2upgrade.service.impl;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import com.tear.upgrade.t1tot2upgrade.repository.RoleRepository;
import com.tear.upgrade.t1tot2upgrade.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role createRole(Role role) {
        return roleRepository.save(role);
    }
}
