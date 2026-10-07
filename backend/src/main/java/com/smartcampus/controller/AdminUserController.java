package com.smartcampus.controller;

import com.smartcampus.model.User;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AdminUserController(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(Objects.requireNonNull(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with that email already exists.");
        }
        ResponseEntity<?> validation = validateRole(user);
        if (validation != null) return validation;
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User updatedData) {
        return userRepository.findById(Objects.requireNonNull(id)).map(existingUser -> {
            if (updatedData.getEmail() != null && !updatedData.getEmail().equals(existingUser.getEmail())
                    && userRepository.findByEmail(updatedData.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("A user with that email already exists.");
            }
            if (updatedData.getRole() != null) {
                ResponseEntity<?> validation = validateRole(updatedData);
                if (validation != null) return validation;
                existingUser.setRole(updatedData.getRole());
            }
            if (updatedData.getName() != null) existingUser.setName(updatedData.getName());
            if (updatedData.getEmail() != null) existingUser.setEmail(updatedData.getEmail());
            if (updatedData.getPasswordHash() != null) existingUser.setPasswordHash(updatedData.getPasswordHash());
            if (updatedData.getCreatedAt() != null) existingUser.setCreatedAt(updatedData.getCreatedAt());
            return ResponseEntity.ok((Object) userRepository.save(existingUser));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(Objects.requireNonNull(id))) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> validateRole(User user) {
        if (user.getRole() == null || user.getRole().getId() == null
                || !roleRepository.existsById(user.getRole().getId())) {
            return ResponseEntity.badRequest().body("Role does not exist.");
        }
        return null;
    }
}
