package com.tear.upgrade.t1tot2upgrade.repository;

import com.tear.upgrade.t1tot2upgrade.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(String roleName);

    Boolean existsByRoleName(String roleName);
}
