package com.jobrecuruitmentplatform.config;

import com.jobrecuruitmentplatform.model.User;
import com.jobrecuruitmentplatform.model.UserRole;
import com.jobrecuruitmentplatform.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String adminEmail = "admin@example.com";

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("password123")); // encrypt password
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(UserRole.ADMIN);

            userRepository.save(admin);
            System.out.println("✅ Admin user created: " + adminEmail);
        } else {
            System.out.println("ℹ️ Admin user already exists: " + adminEmail);
        }
    }
}
