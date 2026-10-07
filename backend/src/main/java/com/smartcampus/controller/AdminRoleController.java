package com.smartcampus.controller;

import com.smartcampus.model.Role;
import com.smartcampus.repository.RoleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/roles")
public class AdminRoleController {

    private final RoleRepository roleRepository;

    public AdminRoleController(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createRole(@RequestBody Role role) {
        if (roleRepository.findByRoleName(role.getRoleName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A role with that name already exists.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(roleRepository.save(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody Role updatedData) {
        return roleRepository.findById(Objects.requireNonNull(id)).map(existingRole -> {
            if (updatedData.getRoleName() != null
                    && !updatedData.getRoleName().equals(existingRole.getRoleName())
                    && roleRepository.findByRoleName(updatedData.getRoleName()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A role with that name already exists.");
            }
            if (updatedData.getRoleName() != null) existingRole.setRoleName(updatedData.getRoleName());
            if (updatedData.getDescription() != null) existingRole.setDescription(updatedData.getDescription());
            return ResponseEntity.ok((Object) roleRepository.save(existingRole));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (!roleRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
