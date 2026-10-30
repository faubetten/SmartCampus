package com.smartcampus.controller;
import com.smartcampus.dto.ForgotPasswordRequestDto;
import com.smartcampus.dto.ForgotPasswordResponseDto;
import com.smartcampus.dto.JwtResponseDto;
import com.smartcampus.dto.LoginRequestDto;
import com.smartcampus.dto.RegisterRequestDto;
import com.smartcampus.dto.ResetPasswordRequestDto;
import com.smartcampus.model.Role;
import com.smartcampus.model.User;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.UserRepository;
import com.smartcampus.security.JwtTokenProvider;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;



@RestController
@RequestMapping("/api/auth")
// Handles account creation and login before protected API requests are made.
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // Temporary reset codes are kept in memory and expire after a short period.
    private final Map<String, PasswordResetToken> resetTokenStore = new ConcurrentHashMap<>();

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

    private static class PasswordResetToken {
        String token;
        LocalDateTime expiryTime;

        PasswordResetToken(String token, LocalDateTime expiryTime) {
            this.token = token;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    // --- Endpoints ---

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {
        System.out.println("[HIT] /api/auth/login reached for email: " + loginRequest.email);


        if (loginRequest.email == null || loginRequest.email.isBlank() || loginRequest.password == null || loginRequest.password.isBlank()) {
            return ResponseEntity.badRequest().body("Error: Email and password must be provided.");
        }

        if (!loginRequest.email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Error: Invalid email format.");
        }

        try {
            // Spring Security checks the password before a token is issued.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email,
                            loginRequest.password
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            System.out.println("[SUCCESS] JWT generated successfully for: " + loginRequest.email);
            return ResponseEntity.ok(new JwtResponseDto(jwt));

        } catch (Exception e) {
            System.out.println("[ERROR] Authentication failed for " + loginRequest.email + ": " + e.getMessage());
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequestDto registerRequest) {
        // Public registration always creates a least-privileged student account.
        if (userRepository.findByEmail(registerRequest.email).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        if (registerRequest.password.length() < 8) {
            return ResponseEntity.badRequest().body("Error: Password must be at least 8 characters long.");
        }

        if (registerRequest.name == null || registerRequest.name.isBlank()) {
            return ResponseEntity.badRequest().body("Error: Name is required.");
        }

        if (registerRequest.email == null || registerRequest.email.isBlank()) {
            return ResponseEntity.badRequest().body("Error: Email is required.");
        }

        if (!registerRequest.email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Error: Invalid email format.");
        }

        if (registerRequest.password == null || registerRequest.password.isBlank()) {
            return ResponseEntity.badRequest().body("Error: Password is required.");
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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        if (request.email == null || request.email.isBlank()) {
            return ResponseEntity.badRequest().body("Error: Email is required.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.email.trim());
        if (userOpt.isEmpty()) {
            // Return 404 if email not found
            return ResponseEntity.status(404).body("Error: No account found with that email address.");
        }

        // Generate a 6-digit numeric OTP valid for 15 minutes
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(15);

        resetTokenStore.put(request.email.trim().toLowerCase(), new PasswordResetToken(otp, expiry));

        // In production this would send via JavaMailSender; for development it is logged to console
        System.out.println("==================================================");
        System.out.println("[PASSWORD RESET] OTP generated for: " + request.email);
        System.out.println("[PASSWORD RESET] Code: " + otp + " (Expires in 15 mins)");
        System.out.println("==================================================");

        return ResponseEntity.ok(new ForgotPasswordResponseDto(
                "Password reset code generated. Enter the code below to proceed.",
                otp
        ));
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDto request) {
        if (request.email == null || request.otp == null || request.newPassword == null) {
            return ResponseEntity.badRequest().body("Error: Email, OTP, and new password are required.");
        }

        if (request.newPassword.length() < 8) {
            return ResponseEntity.badRequest().body("Error: Password must be at least 8 characters long.");
        }

        if (!request.email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseEntity.badRequest().body("Error: Invalid email format.");
        }



        String normalizedEmail = request.email.trim().toLowerCase();
        PasswordResetToken resetToken = resetTokenStore.get(normalizedEmail);

        if (resetToken == null) {
            return ResponseEntity.badRequest().body("Error: No active password reset request found for this email.");
        }

        if (resetToken.isExpired()) {
            resetTokenStore.remove(normalizedEmail);
            return ResponseEntity.badRequest().body("Error: Reset code has expired. Please request a new one.");
        }

        System.out.println("[PASSWORD RESET] Attempting to reset password for: " + normalizedEmail);
        System.out.println("[PASSWORD RESET] Provided OTP: " + request.otp);
        System.out.println("[PASSWORD RESET] Expected OTP: " + resetToken.token);

        if (!resetToken.token.equals(request.otp.trim())) {
            return ResponseEntity.badRequest().body("Error: Invalid reset code.");
        }

        // Fetch user, encode the new password, and persist
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new RuntimeException("Error: User record not found."));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword));
        userRepository.save(user);

        // Invalidate OTP after single use
        resetTokenStore.remove(normalizedEmail);

        System.out.println("[PASSWORD RESET] Password successfully updated for: " + normalizedEmail);
        return ResponseEntity.ok("Password has been successfully updated! You can now log in.");
    }
}