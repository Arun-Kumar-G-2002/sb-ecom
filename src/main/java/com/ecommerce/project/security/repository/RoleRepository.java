package com.ecommerce.project.security.repository;

import com.ecommerce.project.enums.AppRole;
import com.ecommerce.project.security.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole appRole);
}
