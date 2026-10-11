package com.smartcampus;

import com.smartcampus.model.Role;
import com.smartcampus.model.User;
import com.smartcampus.repository.RoleRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@RestController
public class SmartCampusApplication {

    // Identifies the backend instance returned by the health endpoint.
    @Value("${INSTANCE_ID:Unknown-Node}")
    private String instanceId;

    // Used by the load-balancer test to show which node handled a request.
    @GetMapping("/health")
    public String healthCheck() {
        return "SmartCampus API is online, handled by: " + instanceId;
    }

    /**
     * Creates demo accounts when the application starts, without duplicating
     * users that are already present in the database.
     */
    @Bean
    CommandLineRunner initDefaultUsers(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Role studentRole = roleRepository.findByRoleName("STUDENT").orElse(null);
            Role professorRole = roleRepository.findByRoleName("PROFESSOR").orElse(null);
            Role adminRole = roleRepository.findByRoleName("ADMIN").orElse(null);

            String defaultPassword = "SecurePass123!";

            // These accounts make the development and demonstration environment usable.
            createIfNotExists(userRepository, passwordEncoder, "João Silva", "joao.silva@student.universidadeeuropeia.pt", defaultPassword, studentRole);
            createIfNotExists(userRepository, passwordEncoder, "Beatriz Costa", "beatriz.costa@student.universidadeeuropeia.pt", defaultPassword, studentRole);
            createIfNotExists(userRepository, passwordEncoder, "Dr. Andreia Artifice", "andreia.artifice@universidadeeuropeia.pt", defaultPassword, professorRole);
            createIfNotExists(userRepository, passwordEncoder, "Prof. Pedro Rosa", "pedro.rosa@universidadeeuropeia.pt", defaultPassword, professorRole);
            createIfNotExists(userRepository, passwordEncoder, "Admin User", "admin@universidadeeuropeia.pt", defaultPassword, adminRole);
        };
    }

    private void createIfNotExists(UserRepository repo, PasswordEncoder encoder, String name, String email, String rawPass, Role role) {
        if (repo.findByEmail(email).isEmpty()) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            // Store only a BCrypt hash; the original password is never persisted.
            user.setPasswordHash(encoder.encode(rawPass));
            user.setRole(role);
            repo.save(user);
            System.out.println("[INIT] Seeded default user with runtime BCrypt hash: " + email);
        }
    }

    public static void main(String[] args) {
        // Starts Spring Boot and registers the application components.
        SpringApplication.run(SmartCampusApplication.class, args);
    }
}