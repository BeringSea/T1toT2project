package com.tear.upgrade.t1tot2upgrade.repository;

import com.tear.upgrade.t1tot2upgrade.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

   Boolean existsByEmail(String email);

}
