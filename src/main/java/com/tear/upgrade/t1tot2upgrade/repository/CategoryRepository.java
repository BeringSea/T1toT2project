package com.tear.upgrade.t1tot2upgrade.repository;

import com.tear.upgrade.t1tot2upgrade.entity.Category;
import com.tear.upgrade.t1tot2upgrade.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByUserId(Long userId, Pageable page);

    Optional<Category> findByUserIdAndId(Long userId, Long expenseId);

    boolean existsByNameAndUserId(String name, Long id);

    Optional<Category> findByNameAndUser(String name, User user);
}
