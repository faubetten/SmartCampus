package com.smartcampus.repository;

import com.smartcampus.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    // Roles are looked up by their stable application name during registration.
    Optional<Role> findByRoleName(String roleName);
}