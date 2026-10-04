package com.smartcampus.controller;

import com.smartcampus.model.Role;
import com.smartcampus.model.User;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String password;
    }

    public static class JwtResponse {
        public String accessToken;
        public String tokenType = "Bearer";

        public JwtResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        System.out.println("[HIT] /api/auth/login reached for email: " + loginRequest.email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email,
                            loginRequest.password
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            System.out.println("[SUCCESS] JWT generated successfully for: " + loginRequest.email);
            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            System.out.println("[ERROR] Authentication failed for " + loginRequest.email + ": " + e.getMessage());
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.email).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        Role studentRole = roleRepository.findByRoleName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Error: Default role not found in database."));

        User newUser = new User();
        newUser.setName(registerRequest.name);
        newUser.setEmail(registerRequest.email);
        newUser.setPasswordHash(passwordEncoder.encode(registerRequest.password));
        newUser.setRole(studentRole);

        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully!");
    }
}