package com.smartcampus.repository;

import com.smartcampus.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Roles are looked up by their stable application name during registration.
    Optional<Role> findByRoleName(String roleName);
}